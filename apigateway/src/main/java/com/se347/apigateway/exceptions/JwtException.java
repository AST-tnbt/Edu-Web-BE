package com.se347.apigateway.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi liên quan đến JWT
 */
public class JwtException extends BaseGatewayException {
    
    public JwtException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "JWT_ERROR");
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, "JWT_ERROR");
    }
    
    public static class JwtTokenMissingException extends JwtException {
        public JwtTokenMissingException() {
            super("JWT token is missing from request");
        }
        
        public JwtTokenMissingException(String message) {
            super("JWT token is missing: " + message);
        }
    }
    
    public static class JwtTokenInvalidException extends JwtException {
        public JwtTokenInvalidException() {
            super("JWT token is invalid");
        }
        
        public JwtTokenInvalidException(String message) {
            super("JWT token is invalid: " + message);
        }
        
        public JwtTokenInvalidException(String message, Throwable cause) {
            super("JWT token is invalid: " + message, cause);
        }
    }
    
    public static class JwtTokenExpiredException extends JwtException {
        public JwtTokenExpiredException() {
            super("JWT token has expired");
        }
        
        public JwtTokenExpiredException(String message) {
            super("JWT token has expired: " + message);
        }
    }
    
    public static class JwtTokenMalformedException extends JwtException {
        public JwtTokenMalformedException() {
            super("JWT token is malformed");
        }
        
        public JwtTokenMalformedException(String message) {
            super("JWT token is malformed: " + message);
        }
        
        public JwtTokenMalformedException(String message, Throwable cause) {
            super("JWT token is malformed: " + message, cause);
        }
    }
    
    public static class JwtSignatureException extends JwtException {
        public JwtSignatureException() {
            super("JWT signature verification failed");
        }
        
        public JwtSignatureException(String message) {
            super("JWT signature verification failed: " + message);
        }
        
        public JwtSignatureException(String message, Throwable cause) {
            super("JWT signature verification failed: " + message, cause);
        }
    }
    
    public static class JwtConfigurationException extends JwtException {
        public JwtConfigurationException(String message) {
            super("JWT configuration error: " + message);
        }
        
        public JwtConfigurationException(String message, Throwable cause) {
            super("JWT configuration error: " + message, cause);
        }
    }
    
    public static class JwtSecretKeyException extends JwtException {
        public JwtSecretKeyException(String message) {
            super("JWT secret key error: " + message);
        }
        
        public JwtSecretKeyException(String message, Throwable cause) {
            super("JWT secret key error: " + message, cause);
        }
    }
}
