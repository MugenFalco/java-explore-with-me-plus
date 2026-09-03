package ewm.common.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDto {

    @Min(0)
    private int from = 0;

    @Min(1)
    private int size = 10;
}
