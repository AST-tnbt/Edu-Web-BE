package com.se347.courseservice.exceptions;

/**
 * Base exception class for all lesson-related exceptions
 */
public class LessonException extends RuntimeException {
    public LessonException(String message) {
        super(message);
    }

    public LessonException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class LessonNotFoundException extends LessonException {
        public LessonNotFoundException(String lessonId) {
            super("Lesson not found with ID: " + lessonId);
        }
    }

    public static class UnauthorizedAccessException extends LessonException {
        public UnauthorizedAccessException(String message) {
            super("Unauthorized access: " + message);
        }
    }
}

