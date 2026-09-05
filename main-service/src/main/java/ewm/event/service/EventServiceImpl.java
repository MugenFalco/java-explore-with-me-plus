package ewm.event.service;

import ewm.category.Category;
import ewm.category.CategoryService;
import ewm.common.dto.PageRequestDto;
import ewm.event.dto.AdminEventSearchParams;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.EventAdminStateAction;
import ewm.event.dto.EventUserStateAction;
import ewm.event.dto.NewEventDto;
import ewm.event.dto.PublicEventSort;
import ewm.event.dto.PublicEventSearchParams;
import ewm.event.dto.UpdateEventAdminRequest;
import ewm.event.dto.UpdateEventUserRequest;
import ewm.event.entity.*;
import ewm.event.mapper.EventMapper;
import ewm.event.repository.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.request.service.RequestService;
import ewm.user.User;
import ewm.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.client.StatsClient;
import stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final StatsClient statsClient;

    private static final LocalDateTime STATS_RANGE_START = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final int EVENT_LEAD_TIME_HOURS = 2;
    private static final int ADMIN_EVENT_LEAD_TIME_HOURS = 1;

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestService requestService;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, PageRequestDto pageRequest) {
        getUser(userId);
        List<Event> events = getPage(new EventPage(pageRequest, Sort.unsorted()),
                pageable -> eventRepository.findAllByInitiatorId(userId, pageable));
        Map<Long, EventMetrics> metrics = metricsFor(eventIdsOf(events));
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, metrics.get(event.getId())))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        validateEventDate(dto.getEventDate());
        User initiator = getUser(userId);
        Category category = getCategory(dto.getCategory());
        Event event = EventMapper.toEvent(dto, new EventCreationContext(category, initiator));
        Event saved = eventRepository.save(event);
        return EventMapper.toEventFullDto(saved, metricsFor(saved.getId()));
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = getUserEventOrThrow(userId, eventId);
        return EventMapper.toEventFullDto(event, metricsFor(eventId));
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(UserEventPath eventPath, UpdateEventUserRequest request) {
        Event event = getUserEventOrThrow(eventPath.getUserId(), eventPath.getEventId());
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменять можно только события в состоянии ожидания модерации или отменённые.");
        }
        if (request.getEventDate() != null) {
            validateEventDate(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(getCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(EventMapper.toLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getStateAction() != null) {
            event.setState(toState(request.getStateAction()));
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        return EventMapper.toEventFullDto(event, metricsFor(event.getId()));
    }

    @Override
    public List<EventFullDto> getAdminEvents(AdminEventSearchParams searchParams) {
        List<Event> events = getPage(new EventPage(searchParams, Sort.unsorted()), pageable -> eventRepository.findAll(
                EventSpecification.byAdminFilters(searchParams), pageable));
        Map<Long, EventMetrics> metrics = metricsFor(eventIdsOf(events));
        return events.stream()
                .map(event -> EventMapper.toEventFullDto(event, metrics.get(event.getId())))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEvent(eventId);
        if (request.getEventDate() != null) {
            validateAdminEventDate(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(getCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(EventMapper.toLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getStateAction() != null) {
            updateAdminState(event, request.getStateAction());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        return EventMapper.toEventFullDto(event, metricsFor(event.getId()));
    }

    @Override
    public List<EventShortDto> getPublicEvents(PublicEventSearchParams searchParams) {
        validateRange(searchParams.getRangeStart(), searchParams.getRangeEnd());
        if (searchParams.getRangeStart() == null && searchParams.getRangeEnd() == null) {
            searchParams.setRangeStart(LocalDateTime.now());
        }
        List<Event> events = getPage(new EventPage(searchParams, toSort(searchParams.getSort())),
                pageable -> eventRepository.findAll(EventSpecification.byPublicFilters(searchParams), pageable));
        Map<Long, EventMetrics> metrics = metricsFor(eventIdsOf(events));
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, metrics.get(event.getId())))
                .toList();
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = getEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с идентификатором " + eventId + " не найдено.");
        }
        return EventMapper.toEventFullDto(event, metricsFor(eventId));
    }


    @Override
    public Set<Event> getEventsByIds(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Set.of();

        List<Event> foundEvents = eventRepository.findAllById(eventIds);

        if (foundEvents.size() != eventIds.size()) {
            throw new NotFoundException("Один или несколько запрошенных событий не найдены в базе данных");
        }

        return new HashSet<>(foundEvents);
    }

    private EventState toState(EventUserStateAction stateAction) {
        return switch (stateAction) {
            case SEND_TO_REVIEW -> EventState.PENDING;
            case CANCEL_REVIEW -> EventState.CANCELED;
        };
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(EVENT_LEAD_TIME_HOURS))) {
            throw new ConflictException("Дата события должна быть не ранее чем через два часа от текущего момента.");
        }
    }

    private void validateAdminEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(ADMIN_EVENT_LEAD_TIME_HOURS))) {
            throw new ConflictException("Дата события должна быть не ранее чем через один час от текущего момента.");
        }
    }

    private List<Event> getPage(EventPage page, Function<PageRequest, Page<Event>> loader) {
        if (page.from < 0 || page.size < 1) {
            throw new ValidationException(
                    "Параметр from не может быть отрицательным, а size должен быть положительным."
            );
        }

        return loader.apply(
                PageRequest.of(
                        page.from / page.size,
                        page.size,
                        page.sort
                )
        ).getContent();
    }

    private void validateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата rangeStart не может быть позже даты rangeEnd.");
        }
    }

    private Sort toSort(PublicEventSort sort) {
        if (sort == PublicEventSort.EVENT_DATE) {
            return Sort.by(Sort.Direction.ASC, "eventDate");
        }
        return Sort.unsorted();
    }

    private void updateAdminState(Event event, EventAdminStateAction stateAction) {
        if (stateAction == EventAdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Нельзя опубликовать событие: оно должно находиться в состоянии ожидания "
                        + "модерации. Текущее состояние: " + event.getState() + ".");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
            return;
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя отменить уже опубликованное событие.");
        }
        event.setState(EventState.CANCELED);
    }

    private User getUser(Long userId) {
        return userService.getEntityById(userId);
    }

    private Category getCategory(Long categoryId) {
        return categoryService.getEntityById(categoryId);
    }

    private Event getUserEventOrThrow(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с идентификатором " + eventId + " не найдено."));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с идентификатором " + eventId + " не найдено."));
    }

    private List<Long> eventIdsOf(List<Event> events) {
        return events.stream().map(Event::getId).toList();
    }

    private EventMetrics metricsFor(Long eventId) {
        return new EventMetrics(requestService.countConfirmed(eventId), viewsFor(eventId));
    }

    private Map<Long, EventMetrics> metricsFor(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> confirmedByEvent = requestService.countConfirmedForEvents(eventIds);
        Map<Long, Long> viewsByEvent = viewsFor(eventIds);
        return eventIds.stream().collect(Collectors.toMap(
                id -> id,
                id -> new EventMetrics(confirmedByEvent.getOrDefault(id, 0L), viewsByEvent.getOrDefault(id, 0L))));
    }

    private static final class EventPage {

        private final int from;
        private final int size;
        private final Sort sort;

        private EventPage(PageRequestDto pageRequest, Sort sort) {
            this.from = pageRequest.getFrom();
            this.size = pageRequest.getSize();
            this.sort = sort;
        }
    }

    private long viewsFor(Long eventId) {
        return viewsFor(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    private Map<Long, Long> viewsFor(List<Long> eventIds) {
        List<String> uris = eventIds.stream().map(this::eventUri).toList();
        List<ViewStatsDto> stats = statsClient.getStats(STATS_RANGE_START, LocalDateTime.now(), uris, true);
        Map<String, Long> hitsByUri = stats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));
        return eventIds.stream().collect(Collectors.toMap(
                id -> id,
                id -> hitsByUri.getOrDefault(eventUri(id), 0L)));
    }

    private String eventUri(Long eventId) {
        return "/events/" + eventId;
    }
}