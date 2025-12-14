package com.se347.courseservice.exceptions;

/**
 * Base exception class for all section-related exceptions
 */
public class SectionException extends RuntimeException {
    public SectionException(String message) {
        super(message);
    }

    public SectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class SectionNotFoundException extends SectionException {
        public SectionNotFoundException(String sectionId) {
            super("Section not found with ID: " + sectionId);
        }
    }

    public static class UnauthorizedAccessException extends SectionException {
        public UnauthorizedAccessException(String message) {
            super("Unauthorized access: " + message);
        }
    }
}


