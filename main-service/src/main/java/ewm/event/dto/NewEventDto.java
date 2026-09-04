package ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 20, max = 2000, message = "длина должна быть от 20 до 2000 символов")
    private String annotation;

    @NotNull(message = "должно быть указано")
    private Long category;

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 20, max = 7000, message = "длина должна быть от 20 до 7000 символов")
    private String description;

    @NotNull(message = "должно быть указано")
    @Future(message = "должно содержать дату, которая еще не наступила")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "должно быть указано")
    @Valid
    private LocationDto location;

    private Boolean paid = false;

    @PositiveOrZero(message = "должно быть неотрицательным")
    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 3, max = 120, message = "длина должна быть от 3 до 120 символов")
    private String title;
}
