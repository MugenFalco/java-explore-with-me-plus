package ewm.event.controller;

import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.PublicEventSearchParams;
import ewm.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@Valid @ModelAttribute PublicEventSearchParams searchParams) {
        return eventService.getPublicEvents(searchParams);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id) {
        return eventService.getPublicEvent(id);
    }
}
