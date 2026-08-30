package stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import stats.dto.EndpointHitDto;
import stats.dto.GetStatsRequestDto;
import stats.dto.ViewStatsDto;
import stats.service.StatsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Сохранение информации о том, что к эндпоинту был запрос: app={}, uri={}, ip={}", endpointHitDto.getApp(),
                endpointHitDto.getUri(), endpointHitDto.getIp());
        statsService.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@Valid GetStatsRequestDto request) {
        log.info("Получение статистики по посещениям: start={}, end={}, uris={}, unique={}",
                request.getStart(), request.getEnd(), request.getUris(), request.isUnique());

        return statsService.getStats(request.getStart(), request.getEnd(),
                request.getUris(), request.isUnique());
    }
}