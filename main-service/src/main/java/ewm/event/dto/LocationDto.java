package ewm.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    @NotNull(message = "должно быть указано")
    private Float lat;

    @NotNull(message = "должно быть указано")
    private Float lon;
}
