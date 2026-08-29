package stats.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String message;
    private final LocalDateTime timestamp;
    private final int status;
    private final String stacktrace;

    public ErrorResponse(String message, int status, String stacktrace) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.stacktrace = stacktrace;
    }
}