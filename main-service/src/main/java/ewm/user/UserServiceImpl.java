package ewm.user;

import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.user.dto.NewUserRequest;
import ewm.user.dto.UserDto;
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
            throw new ConflictException("could not execute statement; constraint [uq_email]");
        }
        User saved = userRepository.save(UserMapper.toUser(request));
        log.info("Зарегистрирован пользователь с id {}", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    @Override
    public List<UserDto> getAll(List<Long> ids, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        Page<User> page = (ids == null || ids.isEmpty())
                ? userRepository.findAll(pageRequest)
                : userRepository.findAllByIdIn(ids, pageRequest);
        return page.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
        log.info("Удалён пользователь с id {}", userId);
    }
}