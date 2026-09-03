package ewm.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return build(HttpStatus.NOT_FOUND, new ErrorDescription("Необходимый объект не найден.", e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.warn("Конфликт: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, new ErrorDescription("Условия для выполнения операции не соблюдены.", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Нарушение целостности данных: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, new ErrorDescription("Нарушено ограничение целостности данных.",
                "Нарушено ограничение целостности данных."));
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, new ErrorDescription("Некорректно составлен запрос.", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> "Поле «" + fe.getField() + "»: " + fe.getDefaultMessage()
                        + ". Переданное значение: " + fe.getRejectedValue())
                .orElse("Ошибка валидации");
        log.warn("Ошибка валидации тела запроса: {}", message);
        return build(HttpStatus.BAD_REQUEST, new ErrorDescription("Некорректно составлен запрос.", message));
    }

    @ExceptionHandler({ConstraintViolationException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception e) {
        log.warn("Некорректный запрос: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, new ErrorDescription("Некорректно составлен запрос.",
                "Проверьте формат и обязательные параметры запроса."));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return build(HttpStatus.NOT_FOUND, new ErrorDescription("Необходимый объект не найден.", "Объект не найден."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoResource(NoResourceFoundException e) {
        log.warn("Маршрут не найден: {}", e.getResourcePath());
        return build(HttpStatus.NOT_FOUND,
                new ErrorDescription("Запрошенный маршрут не найден.", "Запрошенный ресурс не найден."));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception e) {
        log.error("Непредвиденная ошибка", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                new ErrorDescription("Непредвиденная ошибка.", "Произошла непредвиденная ошибка."));
    }

    private ApiError build(HttpStatus status, ErrorDescription description) {
        return new ApiError(List.of(description.message()), description.message(), description.reason(), status.name(),
                LocalDateTime.now());
    }

    private record ErrorDescription(String reason, String message) {
    }
}
