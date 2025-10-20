package com.se347.userservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi service layer
 */
public class ServiceException extends BaseUserException {
    
    public ServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "SERVICE_ERROR");
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "SERVICE_ERROR");
    }
    
    public static class DatabaseException extends ServiceException {
        public DatabaseException(String operation, String message) {
            super("Database error during " + operation + ": " + message);
        }
        
        public DatabaseException(String operation, String message, Throwable cause) {
            super("Database error during " + operation + ": " + message, cause);
        }
    }
    
    public static class ExternalServiceException extends ServiceException {
        public ExternalServiceException(String service, String message) {
            super("External service '" + service + "' error: " + message);
        }
        
        public ExternalServiceException(String service, String message, Throwable cause) {
            super("External service '" + service + "' error: " + message, cause);
        }
    }
    
    public static class ConfigurationException extends ServiceException {
        public ConfigurationException(String config, String message) {
            super("Configuration error for '" + config + "': " + message);
        }
        
        public ConfigurationException(String config, String message, Throwable cause) {
            super("Configuration error for '" + config + "': " + message, cause);
        }
    }
    
    public static class SecurityException extends ServiceException {
        public SecurityException(String message) {
            super("Security error: " + message);
        }
        
        public SecurityException(String message, Throwable cause) {
            super("Security error: " + message, cause);
        }
    }
    
    public static class AuthenticationException extends ServiceException {
        public AuthenticationException(String message) {
            super("Authentication error: " + message);
        }
        
        public AuthenticationException(String message, Throwable cause) {
            super("Authentication error: " + message, cause);
        }
    }
    
    public static class AuthorizationException extends ServiceException {
        public AuthorizationException(String message) {
            super("Authorization error: " + message);
        }
        
        public AuthorizationException(String message, Throwable cause) {
            super("Authorization error: " + message, cause);
        }
    }
    
    public static class HmacValidationException extends ServiceException {
        public HmacValidationException(String message) {
            super("HMAC validation error: " + message);
        }
        
        public HmacValidationException(String message, Throwable cause) {
            super("HMAC validation error: " + message, cause);
        }
    }
    
    public static class JwtValidationException extends ServiceException {
        public JwtValidationException(String message) {
            super("JWT validation error: " + message);
        }
        
        public JwtValidationException(String message, Throwable cause) {
            super("JWT validation error: " + message, cause);
        }
    }
}
