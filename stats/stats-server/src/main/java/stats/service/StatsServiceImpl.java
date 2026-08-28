package stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.dto.EndpointHitDto;
import stats.dto.ViewStatsDto;
import stats.exception.ValidationException;
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
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit hit = EndpointHitMapper.toEntity(endpointHitDto);
        EndpointHit saved = repository.save(hit);
        return EndpointHitMapper.toDto(saved);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("старт не может быть позже конца");
        }

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
