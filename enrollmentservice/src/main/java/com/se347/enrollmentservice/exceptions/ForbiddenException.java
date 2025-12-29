package com.se347.enrollmentservice.exceptions;

/**
 * Exception thrown when user attempts unauthorized access
 * 
 * HTTP STATUS: 403 Forbidden
 * 
 * USAGE:
 * - User authenticated but not authorized
 * - Different from 401 Unauthorized (not authenticated)
 * 
 * EXAMPLES:
 * - Student trying to view another student's enrollment
 * - Instructor trying to view enrollments in other instructor's course
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

