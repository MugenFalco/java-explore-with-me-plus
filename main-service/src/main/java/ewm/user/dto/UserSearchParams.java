package ewm.user.dto;

import ewm.common.dto.PageRequestDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserSearchParams extends PageRequestDto {

    private List<Long> ids;
}
