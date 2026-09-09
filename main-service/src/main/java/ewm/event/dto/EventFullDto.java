package ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ewm.category.dto.CategoryDto;
import ewm.event.entity.EventState;
import ewm.user.dto.UserShortDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long id;
    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Boolean requestModeration;
    private EventState state;
    private String title;
    private Long views;
}
