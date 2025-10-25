package com.eduweb.exception;

import com.eduweb.model.ErrorResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public abstract class BaseServiceException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    protected BaseServiceException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ErrorResponse toErrorResponse(String path) {
        return ErrorResponse.builder()
            .timestamp(java.time.LocalDateTime.now())
            .status(status.value())
            .error(errorCode)
            .message(getMessage())
            .path(path)
            .build();
    }
}