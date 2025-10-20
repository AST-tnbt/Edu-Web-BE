package com.se347.userservice.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===========================================
    // Base User Exception Handler
    // ===========================================
    
    @ExceptionHandler(BaseUserException.class)
    public ResponseEntity<ErrorResponse> handleBaseUserException(BaseUserException ex, WebRequest request) {
        String path = getPath(request);
        logException(ex);
        
        ErrorResponse error = ex.toErrorResponse(path);
        return new ResponseEntity<>(error, ex.getHttpStatus());
    }
    
    // ===========================================
    // Legacy Exception Handlers (for backward compatibility)
    // ===========================================
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        String path = getPath(request);
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(path)
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String path = getPath(request);
        logger.warn("Validation error: {}", ex.getMessage());
        
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message(message)
                .path(path)
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        String path = getPath(request);
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .path(path)
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex, WebRequest request) {
        String path = getPath(request);
        logger.error("Runtime exception occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("RUNTIME_ERROR")
                .message(ex.getMessage())
                .path(path)
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex, WebRequest request) {
        String path = getPath(request);
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("UNEXPECTED_ERROR")
                .message(ex.getMessage())
                .path(path)
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // ===========================================
    // Helper Methods
    // ===========================================
    
    /**
     * Extract path from WebRequest
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
    
    /**
     * Log exception with appropriate level
     */
    private void logException(BaseUserException ex) {
        HttpStatus status = ex.getHttpStatus();
        
        if (status.is4xxClientError()) {
            logger.warn("Client Error {}: {} - {}", status.value(), ex.getErrorCode(), ex.getMessage());
        } else if (status.is5xxServerError()) {
            logger.error("Server Error {}: {} - {}", status.value(), ex.getErrorCode(), ex.getMessage(), ex);
        } else {
            logger.info("User Service Exception {}: {} - {}", status.value(), ex.getErrorCode(), ex.getMessage());
        }
    }
}
