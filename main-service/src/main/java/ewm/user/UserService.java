package ewm.user;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest request);

    List<UserDto> getAll(List<Long> ids, int from, int size);

    void delete(Long userId);
}