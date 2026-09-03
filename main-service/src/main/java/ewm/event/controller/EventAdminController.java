package ewm.event.controller;

import ewm.event.dto.EventFullDto;
import ewm.event.dto.AdminEventSearchParams;
import ewm.event.dto.UpdateEventAdminRequest;
import ewm.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(@Valid @ModelAttribute AdminEventSearchParams searchParams) {
        return eventService.getAdminEvents(searchParams);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateByAdmin(eventId, request);
    }
}
