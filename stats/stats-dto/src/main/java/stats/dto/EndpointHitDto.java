package stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {

    @NotBlank(message = "не должно быть пустым")
    private String app;

    @NotBlank(message = "не должно быть пустым")
    private String uri;

    @NotBlank(message = "не должно быть пустым")
    private String ip;

    @NotNull(message = "должно быть указано")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
