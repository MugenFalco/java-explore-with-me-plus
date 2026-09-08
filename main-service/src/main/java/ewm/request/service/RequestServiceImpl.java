package ewm.request.service;

import ewm.event.entity.Event;
import ewm.event.entity.EventState;
import ewm.event.service.EventLookupService;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.request.dto.*;
import ewm.request.entity.Request;
import ewm.request.entity.RequestStatus;
import ewm.request.mapper.RequestMapper;
import ewm.request.repository.RequestRepository;
import ewm.user.User;
import ewm.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventLookupService eventLookupService;
    private final UserService userService;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User requester = userService.getEntityById(userId);
        Event event = eventLookupService.getEntityById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии.");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии.");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Нельзя добавить повторный запрос.");
        }

        int limit = event.getParticipantLimit();
        if (limit > 0) {
            long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmed >= limit) {
                throw new ConflictException("У события достигнут лимит запросов на участие.");
            }
        }

        RequestStatus status = limit == 0 || !event.isRequestModeration()
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;
        Request request = RequestMapper.toEntity(event, requester, status);

        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userService.getEntityById(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Запрос с идентификатором " + requestId + " не найден."));
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        eventLookupService.getOwnedEventById(userId, eventId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventLookupService.getOwnedEventById(userId, eventId);

        List<Request> requests = requestRepository.findAllById(updateRequest.getRequestIds());

        if (requests.size() != updateRequest.getRequestIds().size()) {
            throw new NotFoundException("Один или несколько запросов на участие не найдены.");
        }

        for (Request r : requests) {
            if (!r.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Запрос с идентификатором " + r.getId()
                        + " не относится к событию " + eventId + ".");
            }
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request должен быть в статусе PENDING");
            }
        }

        if (updateRequest.getStatus() == RequestStatusUpdateAction.REJECTED) {
            requests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
            return new EventRequestStatusUpdateResult(List.of(), toDtoList(requests));
        }

        int limit = event.getParticipantLimit();
        long alreadyConfirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (limit > 0 && alreadyConfirmed >= limit) {
            throw new ConflictException("Достигнут лимит участников");
        }

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        for (Request r : requests) {
            if (limit > 0 && alreadyConfirmed >= limit) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(r);
            } else {
                r.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(r);
                alreadyConfirmed++;
            }
        }

        return new EventRequestStatusUpdateResult(toDtoList(confirmed), toDtoList(rejected));
    }

    @Override
    public long countConfirmed(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public Map<Long, Long> countConfirmedForEvents(List<Long> eventIds) {
        return requestRepository.countConfirmedByEventIds(eventIds).stream()
                .collect(Collectors.toMap(EventConfirmedCount::getEventId, EventConfirmedCount::getConfirmedRequests));
    }

    private List<ParticipationRequestDto> toDtoList(List<Request> requests) {
        return requests.stream().map(RequestMapper::toDto).toList();
    }
}