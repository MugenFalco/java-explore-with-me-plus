package ewm.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return build(HttpStatus.NOT_FOUND, "The required object was not found.", e.getMessage(), e);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.warn("Конфликт: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.", e.getMessage(), e);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Нарушение целостности данных: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, "Integrity constraint has been violated.", e.getMessage(), e);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Incorrectly made request.", e.getMessage(), e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> "Field: " + fe.getField() + ". Error: " + fe.getDefaultMessage() + ". Value: " + fe.getRejectedValue())
                .orElse("Ошибка валидации");
        log.warn("Ошибка валидации тела запроса: {}", message);
        return build(HttpStatus.BAD_REQUEST, "Incorrectly made request.", message, e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return build(HttpStatus.NOT_FOUND, "The required object was not found.", e.getMessage(), e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception e) {
        log.error("Непредвиденная ошибка", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.", e.getMessage(), e);
    }

    private ApiError build(HttpStatus status, String reason, String message, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return new ApiError(List.of(sw.toString()), message, reason, status.name(), LocalDateTime.now());
    }
}