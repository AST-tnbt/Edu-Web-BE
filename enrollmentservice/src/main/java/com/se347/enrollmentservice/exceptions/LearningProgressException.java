package com.se347.enrollmentservice.exceptions;

/**
 * Base exception class for all learning progress-related exceptions
 */
public abstract class LearningProgressException extends RuntimeException {
    
    public LearningProgressException(String message) {
        super(message);
    }

    /**
     * Exception thrown when learning progress is not found
     */
    public static class UnauthorizedAccessException extends LearningProgressException {
        public UnauthorizedAccessException(String message) {
            super("Unauthorized access: " + message);
        }
    }

    public LearningProgressException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception thrown when learning progress is not found
     */
    public static class LearningProgressNotFoundException extends LearningProgressException {
        public LearningProgressNotFoundException(String learningProgressId) {
            super("Learning progress not found with ID: " + learningProgressId);
        }
    }
    
    /**
     * Exception thrown when request data is invalid
     */
    public static class InvalidRequestException extends LearningProgressException {
        public InvalidRequestException(String message) {
            super("Invalid request: " + message);
        }
    }
    
    /**
     * Exception thrown when learning progress already exists for lesson and enrollment
     */
    public static class LearningProgressAlreadyExistsException extends LearningProgressException {
        public LearningProgressAlreadyExistsException(String lessonId, String enrollmentId) {
            super("Learning progress already exists for lesson ID: " + lessonId + " and enrollment ID: " + enrollmentId);
        }
    }
    
    /**
     * Exception thrown when learning progress is in invalid state for operation
     */
    public static class InvalidLearningProgressStateException extends LearningProgressException {
        public InvalidLearningProgressStateException(String message) {
            super("Invalid learning progress state: " + message);
        }
    }
    
    /**
     * Exception thrown when progress percentage is invalid
     */
    public static class InvalidProgressPercentageException extends LearningProgressException {
        public InvalidProgressPercentageException(String message) {
            super("Invalid progress percentage: " + message);
        }
    }
    
    /**
     * Exception thrown when time spent is invalid
     */
    public static class InvalidTimeSpentException extends LearningProgressException {
        public InvalidTimeSpentException(String message) {
            super("Invalid time spent: " + message);
        }
    }
    
    /**
     * Exception thrown when enrollment is not found for learning progress
     */
    public static class EnrollmentNotFoundException extends LearningProgressException {
        public EnrollmentNotFoundException(String enrollmentId) {
            super("Enrollment not found for learning progress: " + enrollmentId);
        }
    }
    
    /**
     * Exception thrown when content is not found
     */
    public static class ContentNotFoundException extends LearningProgressException {
        public ContentNotFoundException(String contentId) {
            super("Content not found: " + contentId);
        }
    }
    
    /**
     * Exception thrown when lesson is not found
     */
    public static class LessonNotFoundException extends LearningProgressException {
        public LessonNotFoundException(String lessonId) {
            super("Lesson not found: " + lessonId);
        }
    }
    
    /**
     * Exception thrown when learning progress cannot be updated
     */
    public static class LearningProgressUpdateException extends LearningProgressException {
        public LearningProgressUpdateException(String message) {
            super("Cannot update learning progress: " + message);
        }
    }
    
    /**
     * Exception thrown when learning progress is already completed
     */
    public static class LearningProgressAlreadyCompletedException extends LearningProgressException {
        public LearningProgressAlreadyCompletedException(String learningProgressId) {
            super("Learning progress is already completed for ID: " + learningProgressId);
        }
    }
    
    /**
     * Exception thrown when access time is invalid
     */
    public static class InvalidAccessTimeException extends LearningProgressException {
        public InvalidAccessTimeException(String message) {
            super("Invalid access time: " + message);
        }
    }
    
    /**
     * Exception thrown when completion data is invalid
     */
    public static class InvalidCompletionDataException extends LearningProgressException {
        public InvalidCompletionDataException(String message) {
            super("Invalid completion data: " + message);
        }
    }

    public static class DuplicateLearningProgressException extends LearningProgressException {
        public DuplicateLearningProgressException(String message) {
            super(message);
        }
    }
}