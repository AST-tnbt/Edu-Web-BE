package com.se347.enrollmentservice.exceptions;

/**
 * Base exception class for all enrollment-related exceptions
 */
public abstract class EnrollmentException extends RuntimeException {
    
    public EnrollmentException(String message) {
        super(message);
    }
    
    public EnrollmentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Exception thrown when an enrollment is not found
     */
    public static class EnrollmentNotFoundException extends EnrollmentException {
        public EnrollmentNotFoundException(String enrollmentId) {
            super("Enrollment not found with ID: " + enrollmentId);
        }
    }
    
    /**
     * Exception thrown when request data is invalid
     */
    public static class InvalidRequestException extends EnrollmentException {
        public InvalidRequestException(String message) {
            super("Invalid request: " + message);
        }
    }
    
    /**
     * Exception thrown when enrollment already exists
     */
    public static class EnrollmentAlreadyExistsException extends EnrollmentException {
        public EnrollmentAlreadyExistsException(String courseId, String studentId) {
            super("Enrollment already exists for course ID: " + courseId + " and student ID: " + studentId);
        }
    }
    
    /**
     * Exception thrown when enrollment is in invalid state for operation
     */
    public static class InvalidEnrollmentStateException extends EnrollmentException {
        public InvalidEnrollmentStateException(String message) {
            super("Invalid enrollment state: " + message);
        }
    }
    
    /**
     * Exception thrown when payment is required but not completed
     */
    public static class PaymentRequiredException extends EnrollmentException {
        public PaymentRequiredException(String message) {
            super("Payment required: " + message);
        }
    }
    
    /**
     * Exception thrown when enrollment access has expired
     */
    public static class EnrollmentExpiredException extends EnrollmentException {
        public EnrollmentExpiredException(String enrollmentId) {
            super("Enrollment access has expired for ID: " + enrollmentId);
        }
    }
    
    /**
     * Exception thrown when course is not available for enrollment
     */
    public static class CourseNotAvailableException extends EnrollmentException {
        public CourseNotAvailableException(String courseId) {
            super("Course is not available for enrollment: " + courseId);
        }
    }
    
    /**
     * Exception thrown when student is not eligible for enrollment
     */
    public static class StudentNotEligibleException extends EnrollmentException {
        public StudentNotEligibleException(String studentId) {
            super("Student is not eligible for enrollment: " + studentId);
        }
    }
    
    /**
     * Exception thrown when enrollment limit is reached
     */
    public static class EnrollmentLimitExceededException extends EnrollmentException {
        public EnrollmentLimitExceededException(String courseId) {
            super("Enrollment limit exceeded for course: " + courseId);
        }
    }

    public static class DuplicateEnrollmentException extends EnrollmentException {
        public DuplicateEnrollmentException(String message) {
            super(message);
        }
    }
    
    public static class InvalidStatusTransitionException extends EnrollmentException {
        public InvalidStatusTransitionException(String message) {
            super(message);
        }
    }
    
    public static class InvalidPaymentStatusTransitionException extends EnrollmentException {
        public InvalidPaymentStatusTransitionException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedAccessException extends EnrollmentException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }
}
