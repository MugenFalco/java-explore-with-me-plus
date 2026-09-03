package ewm.event;

import ewm.category.Category;
import ewm.category.CategoryRepository;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.EventAdminStateAction;
import ewm.event.dto.EventUserStateAction;
import ewm.event.dto.NewEventDto;
import ewm.event.dto.PublicEventSort;
import ewm.event.dto.UpdateEventAdminRequest;
import ewm.event.dto.UpdateEventUserRequest;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.exception.ValidationException;
import ewm.user.User;
import ewm.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final int EVENT_LEAD_TIME_HOURS = 2;
    private static final int ADMIN_EVENT_LEAD_TIME_HOURS = 1;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        getUser(userId);
        return getPage(from, size, Sort.unsorted(), pageable -> eventRepository.findAllByInitiatorId(userId, pageable))
                .stream()
                .map(event -> EventMapper.toEventShortDto(event, 0, 0))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        validateEventDate(dto.getEventDate());
        User initiator = getUser(userId);
        Category category = getCategory(dto.getCategory());
        Event event = EventMapper.toEvent(dto, category, initiator);
        return EventMapper.toEventFullDto(eventRepository.save(event), 0, 0);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        return EventMapper.toEventFullDto(getUserEventOrThrow(userId, eventId), 0, 0);
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = getUserEventOrThrow(userId, eventId);
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
        return EventMapper.toEventFullDto(event, 0, 0);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        return getPage(from, size, Sort.unsorted(), pageable -> eventRepository.findAll(EventSpecification.byAdminFilters(
                        users, states, categories, rangeStart, rangeEnd), pageable))
                .stream()
                .map(event -> EventMapper.toEventFullDto(event, 0, 0))
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
        return EventMapper.toEventFullDto(event, 0, 0);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               boolean onlyAvailable, PublicEventSort sort, int from, int size) {
        validateRange(rangeStart, rangeEnd);
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }
        LocalDateTime effectiveRangeStart = rangeStart;
        return getPage(from, size, toSort(sort), pageable -> eventRepository.findAll(EventSpecification.byPublicFilters(
                        text, categories, paid, effectiveRangeStart, rangeEnd), pageable))
                .stream()
                // TODO: Person 3 will provide confirmed requests; Person 4 will provide views.
                .map(event -> EventMapper.toEventShortDto(event, 0, 0))
                .toList();
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = getEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с идентификатором " + eventId + " не найдено.");
        }
        // TODO: Person 3 will provide confirmed requests; Person 4 will provide views and endpoint hit.
        return EventMapper.toEventFullDto(event, 0, 0);
    }

    private EventState toState(EventUserStateAction stateAction) {
        return switch (stateAction) {
            case SEND_TO_REVIEW -> EventState.PENDING;
            case CANCEL_REVIEW -> EventState.CANCELED;
        };
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(EVENT_LEAD_TIME_HOURS))) {
            throw new ValidationException("Дата события должна быть не ранее чем через два часа от текущего момента.");
        }
    }

    private void validateAdminEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(ADMIN_EVENT_LEAD_TIME_HOURS))) {
            throw new ValidationException("Дата события должна быть не ранее чем через один час от текущего момента.");
        }
    }

    private List<Event> getPage(int from, int size, Sort sort, Function<PageRequest, Page<Event>> loader) {
        if (from < 0 || size < 1) {
            throw new ValidationException("Параметр from не может быть отрицательным, а size должен быть положительным.");
        }
        long requested = (long) from + size;
        int fetchSize = requested > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) requested;
        return loader.apply(PageRequest.of(0, fetchSize, sort)).getContent().stream()
                .skip(from)
                .limit(size)
                .toList();
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
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с идентификатором " + userId + " не найден."));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с идентификатором " + categoryId + " не найдена."));
    }

    private Event getUserEventOrThrow(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с идентификатором " + eventId + " не найдено."));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с идентификатором " + eventId + " не найдено."));
    }
}
