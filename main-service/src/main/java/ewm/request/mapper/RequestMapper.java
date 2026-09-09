package ewm.request.mapper;

import ewm.event.entity.Event;
import ewm.request.dto.ParticipationRequestDto;
import ewm.request.entity.Request;
import ewm.request.entity.RequestStatus;
import ewm.user.User;

import java.time.LocalDateTime;

public final class RequestMapper {

    private RequestMapper() {
    }

    public static ParticipationRequestDto toDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }

    public static Request toEntity(Event event, User requester, RequestStatus status) {
        Request request = new Request();
        request.setEvent(event);
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
        request.setStatus(status);
        return request;
    }
}