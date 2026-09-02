package ewm.event;

import ewm.category.Category;
import ewm.category.CategoryMapper;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.EventShortDto;
import ewm.event.dto.LocationDto;
import ewm.event.dto.NewEventDto;
import ewm.user.User;
import ewm.user.UserMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {

    public static Event toEvent(NewEventDto dto, Category category, User initiator) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategory(category);
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setInitiator(initiator);
        event.setLocation(toLocation(dto.getLocation()));
        event.setPaid(Boolean.TRUE.equals(dto.getPaid()));
        event.setParticipantLimit(dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit());
        event.setRequestModeration(!Boolean.FALSE.equals(dto.getRequestModeration()));
        event.setState(EventState.PENDING);
        event.setTitle(dto.getTitle());
        event.setCreatedOn(LocalDateTime.now());
        return event;
    }

    public static EventFullDto toEventFullDto(Event event, long confirmedRequests, long views) {
        return new EventFullDto(
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                confirmedRequests,
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
                views
        );
    }

    public static EventShortDto toEventShortDto(Event event, long confirmedRequests, long views) {
        return new EventShortDto(
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                confirmedRequests,
                event.getEventDate(),
                event.getId(),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.isPaid(),
                event.getTitle(),
                views
        );
    }

    public static Location toLocation(LocationDto dto) {
        return new Location(dto.getLat(), dto.getLon());
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}
