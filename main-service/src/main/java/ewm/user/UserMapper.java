package ewm.user;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
import ewm.user.dto.UserShortDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static User toUser(NewUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return user;
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}