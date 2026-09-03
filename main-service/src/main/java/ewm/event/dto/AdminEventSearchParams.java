package ewm.event.dto;

import ewm.common.dto.PageRequestDto;
import ewm.event.entity.EventState;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AdminEventSearchParams extends PageRequestDto {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private List<Long> users;

    private List<EventState> states;

    private List<Long> categories;

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime rangeEnd;
}
