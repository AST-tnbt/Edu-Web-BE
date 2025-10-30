package com.se347.enrollmentservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the enrollment service
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle enrollment not found exceptions
     */
    @ExceptionHandler(EnrollmentException.EnrollmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEnrollmentNotFoundException(
            EnrollmentException.EnrollmentNotFoundException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Enrollment Not Found");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle invalid request exceptions
     */
    @ExceptionHandler(EnrollmentException.InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequestException(
            EnrollmentException.InvalidRequestException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid Request");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle enrollment already exists exceptions
     */
    @ExceptionHandler(EnrollmentException.EnrollmentAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEnrollmentAlreadyExistsException(
            EnrollmentException.EnrollmentAlreadyExistsException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Enrollment Already Exists");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle invalid enrollment state exceptions
     */
    @ExceptionHandler(EnrollmentException.InvalidEnrollmentStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidEnrollmentStateException(
            EnrollmentException.InvalidEnrollmentStateException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid Enrollment State");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle payment required exceptions
     */
    @ExceptionHandler(EnrollmentException.PaymentRequiredException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentRequiredException(
            EnrollmentException.PaymentRequiredException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Payment Required");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.PAYMENT_REQUIRED.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYMENT_REQUIRED);
    }

    /**
     * Handle enrollment expired exceptions
     */
    @ExceptionHandler(EnrollmentException.EnrollmentExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleEnrollmentExpiredException(
            EnrollmentException.EnrollmentExpiredException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Enrollment Expired");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.GONE.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.GONE);
    }

    /**
     * Handle course not available exceptions
     */
    @ExceptionHandler(EnrollmentException.CourseNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleCourseNotAvailableException(
            EnrollmentException.CourseNotAvailableException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Course Not Available");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle student not eligible exceptions
     */
    @ExceptionHandler(EnrollmentException.StudentNotEligibleException.class)
    public ResponseEntity<Map<String, Object>> handleStudentNotEligibleException(
            EnrollmentException.StudentNotEligibleException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Student Not Eligible");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle enrollment limit exceeded exceptions
     */
    @ExceptionHandler(EnrollmentException.EnrollmentLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleEnrollmentLimitExceededException(
            EnrollmentException.EnrollmentLimitExceededException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Enrollment Limit Exceeded");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Handle validation exceptions from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        List<Map<String, Object>> validationErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    Map<String, Object> validationError = new HashMap<>();
                    validationError.put("field", ((FieldError) error).getField());
                    validationError.put("message", error.getDefaultMessage());
                    validationError.put("rejectedValue", ((FieldError) error).getRejectedValue());
                    return validationError;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", "Request validation failed");
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("validationErrors", validationErrors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handle method argument type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        Class<?> requiredTypeClass = ex.getRequiredType();
        String requiredType = requiredTypeClass != null ? 
            requiredTypeClass.getSimpleName() : "unknown";
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), requiredType);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Type Mismatch");
        errorResponse.put("message", message);
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle entity not found exceptions
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Entity Not Found");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Illegal Argument");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred: " + ex.getMessage());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
