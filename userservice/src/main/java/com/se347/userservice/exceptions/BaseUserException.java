package com.se347.userservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base exception class cho User Service
 * 
 * Tất cả các exception trong User Service sẽ extend từ class này
 * để có cấu trúc thống nhất và dễ quản lý
 */
public abstract class BaseUserException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final long timestamp;
    
    public BaseUserException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }
    
    public BaseUserException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Tạo error response object
     */
    public ErrorResponse toErrorResponse(String path) {
        return ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(getHttpStatus().value())
                .error(getErrorCode())
                .message(getMessage())
                .path(path)
                .build();
    }
}
