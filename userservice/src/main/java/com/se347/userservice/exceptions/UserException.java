package com.se347.userservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi liên quan đến User operations
 */
public class UserException extends BaseUserException {
    
    public UserException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "USER_ERROR");
    }
    
    public UserException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "USER_ERROR");
    }
    
    public static class UserNotFoundException extends UserException {
        public UserNotFoundException(String userId) {
            super("User not found with ID: " + userId);
        }
        
        public UserNotFoundException(String field, String value) {
            super("User not found with " + field + ": " + value);
        }
    }
    
    public static class UserProfileNotFoundException extends UserException {
        public UserProfileNotFoundException(String userId) {
            super("User profile not found for user ID: " + userId);
        }
        
        public UserProfileNotFoundException(String field, String value) {
            super("User profile not found with " + field + ": " + value);
        }
    }
    
    public static class UserAlreadyExistsException extends UserException {
        public UserAlreadyExistsException(String field, String value) {
            super("User already exists with " + field + ": " + value);
        }
        
        public UserAlreadyExistsException(String message) {
            super("User already exists: " + message);
        }
    }
    
    public static class UserProfileAlreadyExistsException extends UserException {
        public UserProfileAlreadyExistsException(String userId) {
            super("User profile already exists for user ID: " + userId);
        }
    }
    
    public static class UserInactiveException extends UserException {
        public UserInactiveException(String userId) {
            super("User is inactive: " + userId);
        }
    }
    
    public static class UserBlockedException extends UserException {
        public UserBlockedException(String userId) {
            super("User is blocked: " + userId);
        }
    }
    
    public static class UserPermissionDeniedException extends UserException {
        public UserPermissionDeniedException(String userId, String action) {
            super("Permission denied for user " + userId + " to perform action: " + action);
        }
        
        public UserPermissionDeniedException(String message) {
            super("Permission denied: " + message);
        }
    }
    
    public static class UserDataCorruptedException extends UserException {
        public UserDataCorruptedException(String userId) {
            super("User data is corrupted for user ID: " + userId);
        }
        
        public UserDataCorruptedException(String message, Throwable cause) {
            super("User data is corrupted: " + message, cause);
        }
    }

    public static class UnauthorizedAccessException extends UserException {
        public UnauthorizedAccessException(String message) {
            super("Unauthorized access: " + message);
        }
    }
}
