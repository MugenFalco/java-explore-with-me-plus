package stats.mapper;

import stats.dto.EndpointHitDto;
import stats.model.EndpointHit;

public final class EndpointHitMapper {

    private EndpointHitMapper() {
    }

    public static EndpointHit toEntity(EndpointHitDto dto) {
        EndpointHit hit = new EndpointHit();
        hit.setApp(dto.getApp());
        hit.setUri(dto.getUri());
        hit.setIp(dto.getIp());
        hit.setTimestamp(dto.getTimestamp());
        return hit;
    }

}