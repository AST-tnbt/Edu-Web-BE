package com.se347.userservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi validation
 */
public class ValidationException extends BaseUserException {
    
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
    
    public static class FieldValidationException extends ValidationException {
        private final String field;
        private final String value;
        
        public FieldValidationException(String field, String value, String message) {
            super("Validation failed for field '" + field + "' with value '" + value + "': " + message);
            this.field = field;
            this.value = value;
        }
        
        public FieldValidationException(String field, String message) {
            super("Validation failed for field '" + field + "': " + message);
            this.field = field;
            this.value = null;
        }
        
        public String getField() {
            return field;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public static class RequiredFieldException extends ValidationException {
        public RequiredFieldException(String field) {
            super("Required field '" + field + "' is missing");
        }
        
        public RequiredFieldException(String field, String message) {
            super("Required field '" + field + "' is missing: " + message);
        }
    }
    
    public static class InvalidFormatException extends ValidationException {
        public InvalidFormatException(String field, String value, String expectedFormat) {
            super("Invalid format for field '" + field + "' with value '" + value + "'. Expected format: " + expectedFormat);
        }
        
        public InvalidFormatException(String field, String expectedFormat) {
            super("Invalid format for field '" + field + "'. Expected format: " + expectedFormat);
        }
    }
    
    public static class InvalidValueException extends ValidationException {
        public InvalidValueException(String field, String value, String constraint) {
            super("Invalid value for field '" + field + "' with value '" + value + "': " + constraint);
        }
        
        public InvalidValueException(String field, String constraint) {
            super("Invalid value for field '" + field + "': " + constraint);
        }
    }
    
    public static class DataIntegrityException extends ValidationException {
        public DataIntegrityException(String message) {
            super("Data integrity violation: " + message);
        }
        
        public DataIntegrityException(String message, Throwable cause) {
            super("Data integrity violation: " + message, cause);
        }
    }
    
    public static class ConstraintViolationException extends ValidationException {
        public ConstraintViolationException(String constraint, String message) {
            super("Constraint violation '" + constraint + "': " + message);
        }
        
        public ConstraintViolationException(String constraint, String message, Throwable cause) {
            super("Constraint violation '" + constraint + "': " + message, cause);
        }
    }
}
