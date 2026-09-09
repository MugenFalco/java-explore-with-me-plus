package ewm.compilation.dto;

import ewm.common.dto.PageRequestDto;
import lombok.Getter;

@Getter
public class CompilationSearchParams extends PageRequestDto {
    private Boolean pinned;
}
