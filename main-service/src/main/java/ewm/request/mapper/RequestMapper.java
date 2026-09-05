package ewm.request.mapper;

import ewm.request.dto.ParticipationRequestDto;
import ewm.request.entity.Request;

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
}