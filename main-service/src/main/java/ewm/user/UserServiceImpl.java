package ewm.user;

import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
import ewm.user.dto.UserSearchParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с таким адресом электронной почты уже существует.");
        }
        User saved = userRepository.save(UserMapper.toUser(request));
        log.info("Зарегистрирован пользователь с id {}", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    @Override
    public List<UserDto> getAll(UserSearchParams searchParams) {
        PageRequest pageRequest = PageRequest.of(searchParams.getFrom() / searchParams.getSize(),
                searchParams.getSize());
        Page<User> page = (searchParams.getIds() == null || searchParams.getIds().isEmpty())
                ? userRepository.findAll(pageRequest)
                : userRepository.findAllByIdIn(searchParams.getIds(), pageRequest);
        return page.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        getEntityById(userId);
        userRepository.deleteById(userId);
        log.info("Удалён пользователь с id {}", userId);
    }

    @Override
    public User getEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с идентификатором " + userId + " не найден."));
    }
}
