package com.se347.apigateway.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base exception class cho API Gateway
 * 
 * Tất cả các exception trong Gateway sẽ extend từ class này
 * để có cấu trúc thống nhất và dễ quản lý
 */
public abstract class BaseGatewayException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final long timestamp;
    
    public BaseGatewayException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }
    
    public BaseGatewayException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
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
    public ErrorResponse toErrorResponse() {
        return ErrorResponse.builder()
                .error(getErrorCode())
                .message(getMessage())
                .timestamp(getTimestamp())
                .status(getHttpStatus().value())
                .path(getPath())
                .build();
    }
    
    /**
     * Lấy path hiện tại (có thể override trong subclass)
     */
    protected String getPath() {
        return "Unknown";
    }
    
    /**
     * Error Response DTO
     */
    public static class ErrorResponse {
        private String error;
        private String message;
        private long timestamp;
        private int status;
        private String path;
        
        private ErrorResponse() {}
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private ErrorResponse response = new ErrorResponse();
            
            public Builder error(String error) {
                response.error = error;
                return this;
            }
            
            public Builder message(String message) {
                response.message = message;
                return this;
            }
            
            public Builder timestamp(long timestamp) {
                response.timestamp = timestamp;
                return this;
            }
            
            public Builder status(int status) {
                response.status = status;
                return this;
            }
            
            public Builder path(String path) {
                response.path = path;
                return this;
            }
            
            public ErrorResponse build() {
                return response;
            }
        }
        
        // Getters
        public String getError() { return error; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getPath() { return path; }
    }
}
