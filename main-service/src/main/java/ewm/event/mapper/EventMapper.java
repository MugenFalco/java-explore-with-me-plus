package ewm.event.mapper;

import ewm.category.CategoryMapper;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.LocationDto;
import ewm.event.dto.NewEventDto;
import ewm.event.entity.*;
import ewm.user.UserMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {

    public static Event toEvent(NewEventDto dto, EventCreationContext context) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategory(context.category());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setInitiator(context.initiator());
        event.setLocation(toLocation(dto.getLocation()));
        event.setPaid(Boolean.TRUE.equals(dto.getPaid()));
        event.setParticipantLimit(dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit());
        event.setRequestModeration(!Boolean.FALSE.equals(dto.getRequestModeration()));
        event.setState(EventState.PENDING);
        event.setTitle(dto.getTitle());
        event.setCreatedOn(LocalDateTime.now());
        return event;
    }

    public static EventFullDto toEventFullDto(Event event, EventMetrics metrics) {
        return new EventFullDto(
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                metrics.confirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                event.getId(),
                UserMapper.toUserShortDto(event.getInitiator()),
                toLocationDto(event.getLocation()),
                event.isPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.isRequestModeration(),
                event.getState(),
                event.getTitle(),
                metrics.views()
        );
    }

    public static EventShortDto toEventShortDto(Event event, EventMetrics metrics) {
        return new EventShortDto(
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                metrics.confirmedRequests(),
                event.getEventDate(),
                event.getId(),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.isPaid(),
                event.getTitle(),
                metrics.views()
        );
    }

    public static Location toLocation(LocationDto dto) {
        return new Location(dto.getLat(), dto.getLon());
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}
