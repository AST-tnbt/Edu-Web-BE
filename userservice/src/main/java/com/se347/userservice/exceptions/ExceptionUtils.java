package com.se347.userservice.exceptions;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Utility class cho exception handling
 */
public class ExceptionUtils {
    
    // Common validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Validate email format
     */
    public static void validateEmail(String email, String fieldName) {
        if (!StringUtils.hasText(email)) {
            throw new ValidationException.RequiredFieldException(fieldName, "Email is required");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException.InvalidFormatException(fieldName, email, "valid email format");
        }
    }
    
    /**
     * Validate phone number format
     */
    public static void validatePhoneNumber(String phoneNumber, String fieldName) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new ValidationException.RequiredFieldException(fieldName, "Phone number is required");
        }
        
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new ValidationException.InvalidFormatException(fieldName, phoneNumber, "valid phone number format");
        }
    }
    
    /**
     * Validate UUID format
     */
    public static void validateUUID(String uuid, String fieldName) {
        if (!StringUtils.hasText(uuid)) {
            throw new ValidationException.RequiredFieldException(fieldName, "UUID is required");
        }
        
        if (!UUID_PATTERN.matcher(uuid).matches()) {
            throw new ValidationException.InvalidFormatException(fieldName, uuid, "valid UUID format");
        }
    }
    
    /**
     * Validate string length
     */
    public static void validateStringLength(String value, String fieldName, int minLength, int maxLength) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException.RequiredFieldException(fieldName, "Field is required");
        }
        
        int length = value.length();
        if (length < minLength) {
            throw new ValidationException.InvalidValueException(fieldName, 
                "Minimum length is " + minLength + " characters");
        }
        
        if (length > maxLength) {
            throw new ValidationException.InvalidValueException(fieldName, 
                "Maximum length is " + maxLength + " characters");
        }
    }
    
    /**
     * Validate required field
     */
    public static void validateRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException.RequiredFieldException(fieldName);
        }
    }
    
    /**
     * Validate required field with custom message
     */
    public static void validateRequired(String value, String fieldName, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException.RequiredFieldException(fieldName, message);
        }
    }
    
    /**
     * Validate non-null object
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException.RequiredFieldException(fieldName, "Field cannot be null");
        }
    }
    
    /**
     * Validate positive number
     */
    public static void validatePositive(Number value, String fieldName) {
        if (value == null) {
            throw new ValidationException.RequiredFieldException(fieldName, "Field cannot be null");
        }
        
        if (value.doubleValue() <= 0) {
            throw new ValidationException.InvalidValueException(fieldName, 
                "Value must be positive");
        }
    }
    
    /**
     * Validate number range
     */
    public static void validateRange(Number value, String fieldName, Number min, Number max) {
        if (value == null) {
            throw new ValidationException.RequiredFieldException(fieldName, "Field cannot be null");
        }
        
        double val = value.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();
        
        if (val < minVal || val > maxVal) {
            throw new ValidationException.InvalidValueException(fieldName, 
                "Value must be between " + minVal + " and " + maxVal);
        }
    }
    
    /**
     * Validate business rule
     */
    public static void validateBusinessRule(boolean condition, String rule, String message) {
        if (!condition) {
            throw new BusinessException.BusinessRuleViolationException(rule, message);
        }
    }
    
    /**
     * Validate operation is allowed
     */
    public static void validateOperationAllowed(boolean allowed, String operation, String reason) {
        if (!allowed) {
            throw new BusinessException.OperationNotAllowedException(operation, reason);
        }
    }
    
    /**
     * Check if user exists and is active
     */
    public static void validateUserActive(String userId, boolean exists, boolean active) {
        if (!exists) {
            throw new UserException.UserNotFoundException(userId);
        }
        
        if (!active) {
            throw new UserException.UserInactiveException(userId);
        }
    }
    
    /**
     * Check if user profile exists
     */
    public static void validateUserProfileExists(String userId, boolean exists) {
        if (!exists) {
            throw new UserException.UserProfileNotFoundException(userId);
        }
    }
    
    /**
     * Validate HMAC signature
     */
    public static void validateHmacSignature(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new ServiceException.HmacValidationException(message);
        }
    }
    
    /**
     * Validate JWT token
     */
    public static void validateJwtToken(boolean valid, String message) {
        if (!valid) {
            throw new ServiceException.JwtValidationException(message);
        }
    }
    
    /**
     * Safe string conversion
     */
    public static String safeToString(Object value) {
        return value != null ? value.toString() : "null";
    }
    
    /**
     * Get field name from method name
     */
    public static String getFieldName(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        }
        return methodName;
    }
}
