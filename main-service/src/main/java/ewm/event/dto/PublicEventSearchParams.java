package ewm.event.dto;

import ewm.common.dto.PageRequestDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PublicEventSearchParams extends PageRequestDto {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private String text;

    private List<Long> categories;

    private Boolean paid;

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime rangeEnd;

    private boolean onlyAvailable;

    private PublicEventSort sort;
}
