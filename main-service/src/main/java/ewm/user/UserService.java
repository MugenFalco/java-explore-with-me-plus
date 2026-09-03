package ewm.user;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
import ewm.user.dto.UserSearchParams;

import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest request);

    List<UserDto> getAll(UserSearchParams searchParams);

    User getEntityById(Long userId);

    void delete(Long userId);
}
