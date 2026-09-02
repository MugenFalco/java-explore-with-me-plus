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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size))
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
            throw new ConflictException("Only pending or canceled events can be changed");
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
        return eventRepository.findAll(EventSpecification.byAdminFilters(
                        users, states, categories, rangeStart, rangeEnd), PageRequest.of(from / size, size))
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
        PageRequest pageRequest = PageRequest.of(from / size, size, toSort(sort));
        return eventRepository.findAll(EventSpecification.byPublicFilters(
                        text, categories, paid, rangeStart, rangeEnd), pageRequest)
                .stream()
                // TODO: Person 3 will provide confirmed requests; Person 4 will provide views.
                .map(event -> EventMapper.toEventShortDto(event, 0, 0))
                .toList();
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId) {
        Event event = getEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
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
            throw new ConflictException("Field: eventDate. Error: must be at least two hours from now");
        }
    }

    private void validateAdminEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(ADMIN_EVENT_LEAD_TIME_HOURS))) {
            throw new ConflictException("Event date must be at least one hour from now");
        }
    }

    private void validateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("rangeStart must not be after rangeEnd");
        }
    }

    private Sort toSort(PublicEventSort sort) {
        if (sort == PublicEventSort.EVENT_DATE) {
            return Sort.by(Sort.Direction.ASC, "eventDate");
        }
        // Sorting by views will be completed with the stats integration.
        return Sort.unsorted();
    }

    private void updateAdminState(Event event, EventAdminStateAction stateAction) {
        if (stateAction == EventAdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish the event because it's not in the right state: "
                        + event.getState());
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
            return;
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot reject the event because it has already been published");
        }
        event.setState(EventState.CANCELED);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private Event getUserEventOrThrow(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
