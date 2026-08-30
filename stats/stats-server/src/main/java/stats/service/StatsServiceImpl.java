package stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.dto.EndpointHitDto;
import stats.dto.ViewStatsDto;
import stats.mapper.EndpointHitMapper;
import stats.model.EndpointHit;
import stats.repository.EndpointHitRepository;

import java.time.LocalDateTime;
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
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        boolean hasUris = uris != null && !uris.isEmpty();

        if (hasUris) {
            return unique
                    ? repository.findUniqueStatsByUris(start, end, uris)
                    : repository.findStatsByUris(start, end, uris);
        }

        return unique
                ? repository.findUniqueStats(start, end)
                : repository.findStats(start, end);
    }
}
