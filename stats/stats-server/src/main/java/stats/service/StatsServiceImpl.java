package stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.dto.EndpointHitDto;
import stats.dto.GetStatsRequestDto;
import stats.dto.ViewStatsDto;
import stats.mapper.EndpointHitMapper;
import stats.model.EndpointHit;
import stats.repository.EndpointHitRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final EndpointHitRepository repository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit hit = EndpointHitMapper.toEntity(endpointHitDto);
        repository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(GetStatsRequestDto request) {
        boolean hasUris = request.getUris() != null && !request.getUris().isEmpty();

        if (hasUris) {
            return request.isUnique()
                    ? repository.findUniqueStatsByUris(request.getStart(), request.getEnd(), request.getUris())
                    : repository.findStatsByUris(request.getStart(), request.getEnd(), request.getUris());
        }

        return request.isUnique()
                ? repository.findUniqueStats(request.getStart(), request.getEnd())
                : repository.findStats(request.getStart(), request.getEnd());
    }
}
