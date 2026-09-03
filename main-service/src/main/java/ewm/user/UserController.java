package ewm.user;

import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
import ewm.user.dto.UserSearchParams;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest request) {
        log.info("Регистрация пользователя: {}", request.getEmail());
        return userService.create(request);
    }

    @GetMapping
    public List<UserDto> getAll(@Valid @ModelAttribute UserSearchParams searchParams) {
        log.info("Получение пользователей: ids={}, from={}, size={}", searchParams.getIds(),
                searchParams.getFrom(), searchParams.getSize());
        return userService.getAll(searchParams);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("Удаление пользователя с id {}", userId);
        userService.delete(userId);
    }
}
