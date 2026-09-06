package ewm.event.controller;

import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.PublicEventSearchParams;
import ewm.event.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stats.client.StatsClient;
import stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {
    private static final String APP_NAME = "ewm-main-service";

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

    private void recordHit(HttpServletRequest request) {
        statsClient.hit(new EndpointHitDto(APP_NAME, request.getRequestURI(),
                request.getRemoteAddr(), LocalDateTime.now()));
    }
}
