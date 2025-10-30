package com.se347.courseservice.exceptions;

/**
 * Base exception class for all course-related exceptions
 */
public class CourseException extends RuntimeException {
    
    public CourseException(String message) {
        super(message);
    }
    
    public CourseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Exception thrown when a course is not found
     */
    public static class CourseNotFoundException extends CourseException {
        public CourseNotFoundException(String courseId) {
            super("Course not found with ID: " + courseId);
        }
    }
    
    /**
     * Exception thrown when a category is not found
     */
    public static class CategoryNotFoundException extends CourseException {
        public CategoryNotFoundException(String categoryName) {
            super("Category not found with name: " + categoryName);
        }
    }
    
    /**
     * Exception thrown when a lesson is not found
     */
    public static class LessonNotFoundException extends CourseException {
        public LessonNotFoundException(String lessonId) {
            super("Lesson not found with ID: " + lessonId);
        }
    }
    
    /**
     * Exception thrown when content is not found
     */
    public static class ContentNotFoundException extends CourseException {
        public ContentNotFoundException(String contentId) {
            super("Content not found with ID: " + contentId);
        }
    }
    
    /**
     * Exception thrown when request data is invalid
     */
    public static class InvalidRequestException extends CourseException {
        public InvalidRequestException(String message) {
            super("Invalid request: " + message);
        }
    }
    
    /**
     * Exception thrown when user is not authorized to perform an action
     */
    public static class UnauthorizedAccessException extends CourseException {
        public UnauthorizedAccessException(String message) {
            super("Unauthorized access: " + message);
        }
    }
    
    /**
     * Exception thrown when a course already exists
     */
    public static class CourseAlreadyExistsException extends CourseException {
        public CourseAlreadyExistsException(String title) {
            super("Course already exists with title: " + title);
        }
    }
    
    /**
     * Exception thrown when a category already exists
     */
    public static class CategoryAlreadyExistsException extends CourseException {
        public CategoryAlreadyExistsException(String categoryName) {
            super("Category already exists with name: " + categoryName);
        }
    }
    
    /**
     * Exception thrown when there's a database constraint violation
     */
    public static class DatabaseConstraintViolationException extends CourseException {
        public DatabaseConstraintViolationException(String message) {
            super("Database constraint violation: " + message);
        }
    }
    
    /**
     * Exception thrown when there's an internal server error
     */
    public static class InternalServerErrorException extends CourseException {
        public InternalServerErrorException(String message) {
            super("Internal server error: " + message);
        }
        
        public InternalServerErrorException(String message, Throwable cause) {
            super("Internal server error: " + message, cause);
        }
    }
}
