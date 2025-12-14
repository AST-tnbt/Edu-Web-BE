package com.se347.enrollmentservice.exceptions;

/**
 * Base exception class for all course progress-related exceptions
 */
public abstract class CourseProgressException extends RuntimeException {
    
    public CourseProgressException(String message) {
        super(message);
    }
    
    public CourseProgressException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Exception thrown when user does not have access to course progress
     */
    public static class UnauthorizedAccessException extends CourseProgressException {
        public UnauthorizedAccessException(String message) {
            super("Unauthorized access: " + message);
        }
    }

    /**
     * Exception thrown when course progress is not found
     */
    public static class CourseProgressNotFoundException extends CourseProgressException {
        public CourseProgressNotFoundException(String courseProgressId) {
            super("Course progress not found with ID: " + courseProgressId);
        }
    }
    
    /**
     * Exception thrown when request data is invalid
     */
    public static class InvalidRequestException extends CourseProgressException {
        public InvalidRequestException(String message) {
            super("Invalid request: " + message);
        }
    }
    
    /**
     * Exception thrown when course progress already exists for enrollment
     */
    public static class CourseProgressAlreadyExistsException extends CourseProgressException {
        public CourseProgressAlreadyExistsException(String enrollmentId) {
            super("Course progress already exists for enrollment ID: " + enrollmentId);
        }
    }
    
    /**
     * Exception thrown when course progress is in invalid state for operation
     */
    public static class InvalidCourseProgressStateException extends CourseProgressException {
        public InvalidCourseProgressStateException(String message) {
            super("Invalid course progress state: " + message);
        }
    }
    
    /**
     * Exception thrown when progress values are invalid
     */
    public static class InvalidProgressException extends CourseProgressException {
        public InvalidProgressException(String message) {
            super("Invalid progress: " + message);
        }
    }
    
    /**
     * Exception thrown when enrollment is not found for course progress
     */
    public static class EnrollmentNotFoundException extends CourseProgressException {
        public EnrollmentNotFoundException(String enrollmentId) {
            super("Enrollment not found for course progress: " + enrollmentId);
        }
    }
    
    /**
     * Exception thrown when course progress cannot be updated
     */
    public static class CourseProgressUpdateException extends CourseProgressException {
        public CourseProgressUpdateException(String message) {
            super("Cannot update course progress: " + message);
        }
    }
    
    /**
     * Exception thrown when course progress is already completed
     */
    public static class CourseAlreadyCompletedException extends CourseProgressException {
        public CourseAlreadyCompletedException(String courseProgressId) {
            super("Course is already completed for progress ID: " + courseProgressId);
        }
    }
    
    /**
     * Exception thrown when progress percentage is invalid
     */
    public static class InvalidProgressPercentageException extends CourseProgressException {
        public InvalidProgressPercentageException(String message) {
            super("Invalid progress percentage: " + message);
        }
    }
    
    /**
     * Exception thrown when lessons count is invalid
     */
    public static class InvalidLessonsCountException extends CourseProgressException {
        public InvalidLessonsCountException(String message) {
            super("Invalid lessons count: " + message);
        }
    }
}