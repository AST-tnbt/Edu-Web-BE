package com.se347.contentservice.exception;

import com.se347.contentservice.exception.models.ErrorResponse;
import org.springframework.http.HttpStatus;

public abstract class BaseServiceException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    protected BaseServiceException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
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