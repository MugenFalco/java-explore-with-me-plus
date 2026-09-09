package stats.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GetStatsRequestDto {

    @NotNull(message = "должно быть указано")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @NotNull(message = "должно быть указано")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    private List<String> uris;

    private boolean unique;

    @AssertTrue(message = "дата начала не может быть позже даты окончания")
    private boolean isStartBeforeEnd() {
        return start == null || end == null || !start.isAfter(end);
    }
}
