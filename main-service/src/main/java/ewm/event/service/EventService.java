package ewm.event.service;

import ewm.event.entity.UserEventPath;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.AdminEventSearchParams;
import ewm.event.dto.NewEventDto;
import ewm.event.dto.PublicEventSearchParams;
import ewm.event.dto.UpdateEventAdminRequest;
import ewm.event.dto.UpdateEventUserRequest;
import ewm.common.dto.PageRequestDto;

import java.util.List;

public interface EventService {

    List<EventShortDto> getUserEvents(Long userId, PageRequestDto pageRequest);

    EventFullDto create(Long userId, NewEventDto dto);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateByUser(UserEventPath eventPath, UpdateEventUserRequest request);

    List<EventFullDto> getAdminEvents(AdminEventSearchParams searchParams);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getPublicEvents(PublicEventSearchParams searchParams);

    EventFullDto getPublicEvent(Long eventId);
}
