package ewm.event.controller;

import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.PublicEventSearchParams;
import ewm.event.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import stats.client.StatsClient;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventPublicController {

    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getEvents(@Valid @ModelAttribute PublicEventSearchParams searchParams,
                                         HttpServletRequest req) {
        recordHit(req);
        return eventService.getPublicEvents(searchParams);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest req) {
        recordHit(req);
        return eventService.getPublicEvent(id);
    }

    @GetMapping("/top")
    public List<EventShortDto> getTopEvents(@RequestParam(defaultValue = "0") @Min(0) int from,
                                            @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getTopEvents(from, size);
    }

    private void recordHit(HttpServletRequest request) {
        statsClient.hit(
                request.getRequestURI(),
                request.getRemoteAddr()
        );
    }
}
