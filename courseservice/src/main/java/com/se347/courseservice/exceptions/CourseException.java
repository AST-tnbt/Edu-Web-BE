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
     * Exception thrown when a section is not found
     */
    public static class SectionNotFoundException extends CourseException {
        public SectionNotFoundException(String sectionId) {
            super("Section not found with ID: " + sectionId);
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
     * Exception thrown when a lesson with same title already exists in a section
     */
    public static class LessonAlreadyExistsException extends CourseException {
        public LessonAlreadyExistsException(String sectionId, String title) {
            super("Lesson with title '" + title + "' already exists for section '" + sectionId + "'");
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
     * Exception thrown when content already exists (e.g., duplicate title/order within a lesson)
     */
    public static class ContentAlreadyExistsException extends CourseException {
        public ContentAlreadyExistsException(String message) {
            super("Content already exists: " + message);
        }
    }

    /**
     * Exception thrown when content type/status is invalid
     */
    public static class InvalidContentStateException extends CourseException {
        public InvalidContentStateException(String message) {
            super("Invalid content state: " + message);
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

    /**
     * Exception thrown when course invariant is violated
     * Invariant = business rule that must always be true
     */
    public static class CourseInvariantViolationException extends CourseException {
        public CourseInvariantViolationException(String message) {
            super("Course invariant violated: " + message);
        }
    }

    /**
     * Exception thrown when section invariant is violated
     */
    public static class SectionInvariantViolationException extends CourseException {
        public SectionInvariantViolationException(String message) {
            super("Section invariant violated: " + message);
        }
    }

    /**
     * Exception thrown when lesson invariant is violated
     */
    public static class LessonInvariantViolationException extends CourseException {
        public LessonInvariantViolationException(String message) {
            super("Lesson invariant violated: " + message);
        }
    }

    /**
     * Exception thrown when trying to access entity outside aggregate boundary
     */
    public static class AggregateBoundaryViolationException extends CourseException {
        public AggregateBoundaryViolationException(String message) {
            super("Aggregate boundary violated: " + message);
        }
    }
}
