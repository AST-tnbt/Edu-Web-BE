package com.se347.apigateway.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi chung của API Gateway
 */
public class GatewayException extends BaseGatewayException {
    
    public GatewayException(String message, HttpStatus httpStatus) {
        super(message, httpStatus, "GATEWAY_ERROR");
    }
    
    public GatewayException(String message, HttpStatus httpStatus, String errorCode) {
        super(message, httpStatus, errorCode);
    }
    
    public GatewayException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause, httpStatus, "GATEWAY_ERROR");
    }
    
    public GatewayException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause, httpStatus, errorCode);
    }
    
    public static class ServiceUnavailableException extends GatewayException {
        public ServiceUnavailableException(String serviceName) {
            super("Service " + serviceName + " is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
        
        public ServiceUnavailableException(String serviceName, Throwable cause) {
            super("Service " + serviceName + " is unavailable", cause, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
    
    public static class ServiceTimeoutException extends GatewayException {
        public ServiceTimeoutException(String serviceName, long timeoutMs) {
            super("Service " + serviceName + " timeout after " + timeoutMs + "ms", HttpStatus.GATEWAY_TIMEOUT);
        }
        
        public ServiceTimeoutException(String serviceName, long timeoutMs, Throwable cause) {
            super("Service " + serviceName + " timeout after " + timeoutMs + "ms", cause, HttpStatus.GATEWAY_TIMEOUT);
        }
    }
    
    public static class RateLimitExceededException extends GatewayException {
        public RateLimitExceededException() {
            super("Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
        }
        
        public RateLimitExceededException(String message) {
            super("Rate limit exceeded: " + message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
        }
    }
    
    public static class CircuitBreakerOpenException extends GatewayException {
        public CircuitBreakerOpenException(String serviceName) {
            super("Circuit breaker is open for service " + serviceName, HttpStatus.SERVICE_UNAVAILABLE, "CIRCUIT_BREAKER_OPEN");
        }
    }
    
    public static class RequestValidationException extends GatewayException {
        public RequestValidationException(String message) {
            super("Request validation failed: " + message, HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR");
        }
        
        public RequestValidationException(String message, Throwable cause) {
            super("Request validation failed: " + message, cause, HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_ERROR");
        }
    }
    
    public static class RequestBodyException extends GatewayException {
        public RequestBodyException(String message) {
            super("Request body error: " + message, HttpStatus.BAD_REQUEST, "REQUEST_BODY_ERROR");
        }
        
        public RequestBodyException(String message, Throwable cause) {
            super("Request body error: " + message, cause, HttpStatus.BAD_REQUEST, "REQUEST_BODY_ERROR");
        }
    }
    
    public static class FilterException extends GatewayException {
        public FilterException(String filterName, String message) {
            super("Filter " + filterName + " error: " + message, HttpStatus.INTERNAL_SERVER_ERROR, "FILTER_ERROR");
        }
        
        public FilterException(String filterName, String message, Throwable cause) {
            super("Filter " + filterName + " error: " + message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "FILTER_ERROR");
        }
    }
    
    public static class RouteNotFoundException extends GatewayException {
        public RouteNotFoundException(String path) {
            super("No route found for path: " + path, HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND");
        }
    }
    
    public static class ServiceDiscoveryException extends GatewayException {
        public ServiceDiscoveryException(String serviceName) {
            super("Service discovery failed for " + serviceName, HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_DISCOVERY_ERROR");
        }
        
        public ServiceDiscoveryException(String serviceName, Throwable cause) {
            super("Service discovery failed for " + serviceName, cause, HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_DISCOVERY_ERROR");
        }
    }
}
