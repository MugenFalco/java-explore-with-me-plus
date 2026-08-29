package stats.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeParseException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), stacktraceOf(e));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParse(DateTimeParseException e) {
        String message = "старт/конец должны соответствовать паттерну yyyy-MM-dd HH:mm:ss";
        log.warn("Ошибка формата даты: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse(message, HttpStatus.BAD_REQUEST.value(), stacktraceOf(e));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Ошибка валидации");
        log.warn("Ошибка валидации тела запроса: {}", message);
        ErrorResponse response = new ErrorResponse(message, HttpStatus.BAD_REQUEST.value(), stacktraceOf(e));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Непредвиденная ошибка", e);
        String message = e.getMessage() != null ? e.getMessage() : "Произошла непредвиденная ошибка";
        ErrorResponse response = new ErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR.value(), stacktraceOf(e));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String stacktraceOf(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}