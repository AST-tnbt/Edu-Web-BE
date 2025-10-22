# Exception Handling trong User Service

## Tổng quan

User Service được trang bị hệ thống exception handling toàn diện để xử lý các lỗi có thể xảy ra trong quá trình xử lý user operations.

## Cấu trúc Exception Hierarchy

```
BaseUserException (Abstract)
├── UserException
│   ├── UserNotFoundException
│   ├── UserProfileNotFoundException
│   ├── UserAlreadyExistsException
│   ├── UserProfileAlreadyExistsException
│   ├── UserInactiveException
│   ├── UserBlockedException
│   ├── UserPermissionDeniedException
│   └── UserDataCorruptedException
├── ValidationException
│   ├── FieldValidationException
│   ├── RequiredFieldException
│   ├── InvalidFormatException
│   ├── InvalidValueException
│   ├── DataIntegrityException
│   └── ConstraintViolationException
├── BusinessException
│   ├── OperationNotAllowedException
│   ├── ResourceConflictException
│   ├── BusinessRuleViolationException
│   ├── StateTransitionException
│   ├── QuotaExceededException
│   └── DependencyException
└── ServiceException
    ├── DatabaseException
    ├── ExternalServiceException
    ├── ConfigurationException
    ├── SecurityException
    ├── AuthenticationException
    ├── AuthorizationException
    ├── HmacValidationException
    └── JwtValidationException
```

## Các Exception Classes

### 1. **BaseUserException**
- **Mục đích**: Base class cho tất cả exceptions
- **Tính năng**: 
  - HTTP status code
  - Error code
  - Timestamp
  - Error response builder

### 2. **UserException**
- **Mục đích**: Xử lý lỗi liên quan đến User operations
- **HTTP Status**: 400 Bad Request
- **Các loại**:
  - `UserNotFoundException`: User không tồn tại
  - `UserProfileNotFoundException`: User profile không tồn tại
  - `UserAlreadyExistsException`: User đã tồn tại
  - `UserInactiveException`: User không hoạt động

### 3. **ValidationException**
- **Mục đích**: Xử lý lỗi validation
- **HTTP Status**: 400 Bad Request
- **Các loại**:
  - `FieldValidationException`: Lỗi validation field cụ thể
  - `RequiredFieldException`: Field bắt buộc bị thiếu
  - `InvalidFormatException`: Format không hợp lệ
  - `InvalidValueException`: Giá trị không hợp lệ

### 4. **BusinessException**
- **Mục đích**: Xử lý lỗi business logic
- **HTTP Status**: 422 Unprocessable Entity
- **Các loại**:
  - `OperationNotAllowedException`: Operation không được phép
  - `ResourceConflictException`: Xung đột resource
  - `BusinessRuleViolationException`: Vi phạm business rule

### 5. **ServiceException**
- **Mục đích**: Xử lý lỗi service layer
- **HTTP Status**: 500 Internal Server Error
- **Các loại**:
  - `DatabaseException`: Lỗi database
  - `ExternalServiceException`: Lỗi external service
  - `HmacValidationException`: Lỗi HMAC validation

## Exception Handlers

### **GlobalExceptionHandler**
- **Mục đích**: Xử lý tất cả exceptions trong Spring MVC
- **Tích hợp**: Spring Boot MVC
- **Tính năng**:
  - Tự động convert exception thành HTTP response
  - Logging với level phù hợp
  - JSON response format
  - Backward compatibility với legacy exceptions

## Utility Classes

### **ExceptionUtils**
- **Mục đích**: Utility methods cho validation và exception handling
- **Tính năng**:
  - Email validation
  - Phone number validation
  - UUID validation
  - String length validation
  - Business rule validation
  - HMAC/JWT validation

## Cách sử dụng

### 1. **Trong Services**
```java
// Validate input
ExceptionUtils.validateNotNull(userId, "userId");
ExceptionUtils.validateEmail(email, "email");

// Throw business exceptions
if (userExists) {
    throw new UserException.UserAlreadyExistsException("email", email);
}

// Throw validation exceptions
if (name.length() < 2) {
    throw new ValidationException.FieldValidationException("name", name, "Minimum 2 characters");
}
```

### 2. **Trong Controllers**
```java
@GetMapping("/{userId}")
public ResponseEntity<UserProfileResponseDto> getProfile(@PathVariable UUID userId) {
    try {
        UserProfileResponseDto profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    } catch (UserException.UserProfileNotFoundException e) {
        // Exception sẽ được GlobalExceptionHandler xử lý
        throw e;
    }
}
```

### 3. **Error Response Format**
```json
{
  "timestamp": "2023-12-21T10:30:56",
  "status": 400,
  "error": "USER_ERROR",
  "message": "User not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/users/profiles/123e4567-e89b-12d3-a456-426614174000"
}
```

## Validation Examples

### **Email Validation**
```java
ExceptionUtils.validateEmail("user@example.com", "email");
// Throws ValidationException.RequiredFieldException if empty
// Throws ValidationException.InvalidFormatException if invalid format
```

### **Phone Number Validation**
```java
ExceptionUtils.validatePhoneNumber("+84901234567", "phoneNumber");
// Throws ValidationException.RequiredFieldException if empty
// Throws ValidationException.InvalidFormatException if invalid format
```

### **String Length Validation**
```java
ExceptionUtils.validateStringLength(name, "name", 2, 100);
// Throws ValidationException.RequiredFieldException if empty
// Throws ValidationException.InvalidValueException if length invalid
```

### **Business Rule Validation**
```java
ExceptionUtils.validateBusinessRule(
    user.isActive(), 
    "USER_ACTIVE", 
    "User must be active to perform this operation"
);
```

## Logging

### **Log Levels**
- **ERROR**: Server errors (5xx)
- **WARN**: Client errors (4xx)
- **INFO**: General exceptions
- **DEBUG**: Detailed error information

### **Log Format**
```
2023-12-21 10:30:56.123 WARN [userservice] Client Error 400: USER_ERROR - User not found with ID: 123
```

## Configuration

### **Application Properties**
```properties
# Logging level cho exceptions
logging.level.com.se347.userservice.exceptions=DEBUG

# Exception handling
spring.mvc.throw-exception-if-no-handler-found=true
```

## Best Practices

### 1. **Exception Naming**
- Sử dụng tên mô tả rõ ràng
- Kết thúc bằng "Exception"
- Nhóm theo chức năng

### 2. **Error Messages**
- Thông báo lỗi rõ ràng, dễ hiểu
- Không expose thông tin nhạy cảm
- Cung cấp context hữu ích

### 3. **HTTP Status Codes**
- 400: Bad Request (validation errors)
- 404: Not Found (resource not found)
- 409: Conflict (resource conflict)
- 422: Unprocessable Entity (business logic errors)
- 500: Internal Server Error (server errors)

### 4. **Validation Strategy**
- Validate input early
- Use utility methods for common validations
- Provide specific error messages
- Handle edge cases

## Monitoring và Debugging

### **Metrics**
- Exception count by type
- HTTP status distribution
- Response time for error cases

### **Debugging**
- Enable debug logging
- Include stack traces
- Add correlation IDs

## Tóm lại

Hệ thống exception handling trong User Service cung cấp:
- **Cấu trúc rõ ràng**: Phân loại exceptions theo chức năng
- **Xử lý tự động**: Convert exceptions thành HTTP responses
- **Validation utilities**: Methods tiện ích cho validation
- **Logging chi tiết**: Theo dõi và debug dễ dàng
- **Response thống nhất**: Format JSON chuẩn
- **Tích hợp tốt**: Với Spring Boot MVC
