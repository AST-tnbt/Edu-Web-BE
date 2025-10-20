package com.se347.apigateway.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi liên quan đến Security
 */
public class SecurityException extends BaseGatewayException {
    
    public SecurityException(String message) {
        super(message, HttpStatus.FORBIDDEN, "SECURITY_ERROR");
    }
    
    public SecurityException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN, "SECURITY_ERROR");
    }
    
    public static class AccessDeniedException extends SecurityException {
        public AccessDeniedException() {
            super("Access denied");
        }
        
        public AccessDeniedException(String message) {
            super("Access denied: " + message);
        }
    }
    
    public static class InsufficientPermissionsException extends SecurityException {
        public InsufficientPermissionsException() {
            super("Insufficient permissions");
        }
        
        public InsufficientPermissionsException(String requiredRole) {
            super("Insufficient permissions. Required role: " + requiredRole);
        }
        
        public InsufficientPermissionsException(String requiredRole, String userRole) {
            super("Insufficient permissions. Required: " + requiredRole + ", User has: " + userRole);
        }
    }
    
    public static class AuthenticationRequiredException extends SecurityException {
        public AuthenticationRequiredException() {
            super("Authentication required");
        }
        
        public AuthenticationRequiredException(String message) {
            super("Authentication required: " + message);
        }
    }
    
    public static class InvalidCredentialsException extends SecurityException {
        public InvalidCredentialsException() {
            super("Invalid credentials");
        }
        
        public InvalidCredentialsException(String message) {
            super("Invalid credentials: " + message);
        }
    }
    
    public static class AccountLockedException extends SecurityException {
        public AccountLockedException() {
            super("Account is locked");
        }
        
        public AccountLockedException(String message) {
            super("Account is locked: " + message);
        }
    }
    
    public static class AccountExpiredException extends SecurityException {
        public AccountExpiredException() {
            super("Account has expired");
        }
        
        public AccountExpiredException(String message) {
            super("Account has expired: " + message);
        }
    }
    
    public static class SessionExpiredException extends SecurityException {
        public SessionExpiredException() {
            super("Session has expired");
        }
        
        public SessionExpiredException(String message) {
            super("Session has expired: " + message);
        }
    }
    
    public static class SecurityConfigurationException extends SecurityException {
        public SecurityConfigurationException(String message) {
            super("Security configuration error: " + message);
        }
        
        public SecurityConfigurationException(String message, Throwable cause) {
            super("Security configuration error: " + message, cause);
        }
    }
}
