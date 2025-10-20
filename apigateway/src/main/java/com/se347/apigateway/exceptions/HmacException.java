package com.se347.apigateway.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi liên quan đến HMAC
 */
public class HmacException extends BaseGatewayException {
    
    public HmacException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "HMAC_ERROR");
    }
    
    public HmacException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, "HMAC_ERROR");
    }
    
    public static class HmacSigningException extends HmacException {
        public HmacSigningException(String message) {
            super("HMAC signing failed: " + message);
        }
        
        public HmacSigningException(String message, Throwable cause) {
            super("HMAC signing failed: " + message, cause);
        }
    }
    
    public static class HmacValidationException extends HmacException {
        public HmacValidationException(String message) {
            super("HMAC validation failed: " + message);
        }
        
        public HmacValidationException(String message, Throwable cause) {
            super("HMAC validation failed: " + message, cause);
        }
    }
    
    public static class HmacConfigurationException extends HmacException {
        public HmacConfigurationException(String message) {
            super("HMAC configuration error: " + message);
        }
        
        public HmacConfigurationException(String message, Throwable cause) {
            super("HMAC configuration error: " + message, cause);
        }
    }
    
    public static class HmacSecretKeyException extends HmacException {
        public HmacSecretKeyException(String message) {
            super("HMAC secret key error: " + message);
        }
        
        public HmacSecretKeyException(String message, Throwable cause) {
            super("HMAC secret key error: " + message, cause);
        }
    }
}
