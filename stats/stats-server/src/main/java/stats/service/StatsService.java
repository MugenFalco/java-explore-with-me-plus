package stats.service;

import stats.dto.EndpointHitDto;
import stats.dto.GetStatsRequestDto;
import stats.dto.ViewStatsDto;

import java.util.List;

public interface StatsService {

    void saveHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(GetStatsRequestDto request);
}
