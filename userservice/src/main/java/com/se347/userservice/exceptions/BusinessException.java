package com.se347.userservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception cho các lỗi business logic
 */
public class BusinessException extends BaseUserException {
    
    public BusinessException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_ERROR");
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_ERROR");
    }
    
    public static class OperationNotAllowedException extends BusinessException {
        public OperationNotAllowedException(String operation, String reason) {
            super("Operation '" + operation + "' is not allowed: " + reason);
        }
        
        public OperationNotAllowedException(String message) {
            super("Operation not allowed: " + message);
        }
    }
    
    public static class ResourceConflictException extends BusinessException {
        public ResourceConflictException(String resource, String conflict) {
            super("Resource conflict for '" + resource + "': " + conflict);
        }
        
        public ResourceConflictException(String message) {
            super("Resource conflict: " + message);
        }
    }
    
    public static class BusinessRuleViolationException extends BusinessException {
        public BusinessRuleViolationException(String rule, String violation) {
            super("Business rule violation '" + rule + "': " + violation);
        }
        
        public BusinessRuleViolationException(String message) {
            super("Business rule violation: " + message);
        }
    }
    
    public static class StateTransitionException extends BusinessException {
        public StateTransitionException(String fromState, String toState) {
            super("Invalid state transition from '" + fromState + "' to '" + toState + "'");
        }
        
        public StateTransitionException(String fromState, String toState, String reason) {
            super("Invalid state transition from '" + fromState + "' to '" + toState + "': " + reason);
        }
    }
    
    public static class QuotaExceededException extends BusinessException {
        public QuotaExceededException(String resource, int current, int limit) {
            super("Quota exceeded for '" + resource + "': " + current + "/" + limit);
        }
        
        public QuotaExceededException(String message) {
            super("Quota exceeded: " + message);
        }
    }
    
    public static class DependencyException extends BusinessException {
        public DependencyException(String dependency, String message) {
            super("Dependency '" + dependency + "' error: " + message);
        }
        
        public DependencyException(String dependency, String message, Throwable cause) {
            super("Dependency '" + dependency + "' error: " + message, cause);
        }
    }
}
