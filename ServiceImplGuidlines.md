# Service Implementation Guidelines

## Tổng quan

Tài liệu này mô tả quy ước và best practices để viết Service Implementation chuẩn trong dự án EduWeb Backend. Tuân thủ các quy ước này giúp code dễ đọc, dễ maintain và đảm bảo tính nhất quán.

## 1. Cấu trúc tổng thể

### Template cơ bản

```java
@Service
public class XxxServiceImpl implements XxxService {
    
    // ========== Dependencies ==========
    private final XxxRepository xxxRepository;
    private final OtherService otherService; // nếu cần
    
    // Constructor injection
    public XxxServiceImpl(XxxRepository xxxRepository, OtherService otherService) {
        this.xxxRepository = xxxRepository;
        this.otherService = otherService;
    }
    
    // ========== Public API ==========
    // Các method implement từ interface
    
    // ========== Mapping ==========
    // mapToResponse(), mapToEntity()
    
    // ========== Validation ==========
    // validateCreateRequest(), validateUpdateRequest(), validatePatchRequest()
    
    // ========== Business Logic ==========
    // validateBusinessRules(), updateStatus(), calculateProgress()
}
```

### Nguyên tắc tổ chức code

1. **Nhóm theo chức năng**: Public API → Mapping → Validation → Business Logic
2. **Comment phân chia**: Sử dụng comment để phân chia các section
3. **Thứ tự methods**: Theo thứ tự logic từ public đến private helpers

## 2. Dependency Injection

### Constructor Injection (Khuyến nghị)

```java
@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    
    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                               EnrollmentService enrollmentService) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
    }
}
```

### Tránh Field Injection

```java
// ❌ Không nên
@Autowired
private EnrollmentRepository enrollmentRepository;

// ✅ Nên dùng
private final EnrollmentRepository enrollmentRepository;
```

## 3. CRUD Operations Pattern

### CREATE Operation

```java
@Override
public XxxResponseDto createXxx(XxxRequestDto request) {
    // 1. Validate input
    validateCreateRequest(request);
    
    // 2. Validate dependencies
    validateDependencies(request);
    
    // 3. Check business rules
    validateBusinessRulesForCreate(request);
    
    // 4. Create entity
    Entity entity = Entity.builder()
        .field1(request.getField1())
        .field2(request.getField2())
        .build();
    
    // 5. Set audit fields
    entity.onCreate();
    
    // 6. Save and return
    Entity saved = repository.save(entity);
    return mapToResponse(saved);
}
```

### READ Operations

```java
@Override
public XxxResponseDto getXxxById(UUID id) {
    Entity entity = repository.findById(id)
        .orElseThrow(() -> new XxxException.XxxNotFoundException(
            "Entity not found with ID: " + id));
    return mapToResponse(entity);
}

@Override
public List<XxxResponseDto> getXxxByCriteria(UUID criteria) {
    return repository.findByCriteria(criteria)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
}
```

### UPDATE Operation (Full)

```java
@Override
public XxxResponseDto updateXxx(UUID id, XxxRequestDto request) {
    // 1. Validate input
    validateUpdateRequest(id, request);
    
    // 2. Find entity
    Entity entity = repository.findById(id)
        .orElseThrow(() -> new XxxException.XxxNotFoundException(
            "Entity not found with ID: " + id));
    
    // 3. Validate business rules
    validateBusinessRules(entity, request);
    
    // 4. Apply updates
    applyFullUpdate(entity, request);
    
    // 5. Save and return
    entity.onUpdate();
    return mapToResponse(repository.save(entity));
}
```

### PATCH Operation (Partial)

```java
public XxxResponseDto patchXxx(UUID id, XxxRequestDto request) {
    // 1. Validate input
    validatePatchRequest(id, request);
    
    // 2. Find entity
    Entity entity = repository.findById(id)
        .orElseThrow(() -> new XxxException.XxxNotFoundException(
            "Entity not found with ID: " + id));
    
    // 3. Store original values for validation
    Type originalValue = entity.getField();
    
    // 4. Apply partial updates
    applyPartialUpdate(entity, request);
    
    // 5. Validate only changed fields
    validateBusinessRulesForPatch(entity, originalValue);
    
    // 6. Save and return
    entity.onUpdate();
    return mapToResponse(repository.save(entity));
}
```

## 4. Validation Patterns

### Input Validation

```java
private void validateCreateRequest(XxxRequestDto request) {
    if (request == null) {
        throw new XxxException.InvalidRequestException("Request cannot be null");
    }
    if (request.getField() == null) {
        throw new XxxException.InvalidRequestException("Field cannot be null");
    }
    if (request.getValue() < 0) {
        throw new XxxException.InvalidRequestException("Value must be non-negative");
    }
}

private void validateUpdateRequest(UUID id, XxxRequestDto request) {
    if (id == null) {
        throw new XxxException.InvalidRequestException("ID cannot be null");
    }
    if (request == null) {
        throw new XxxException.InvalidRequestException("Request cannot be null");
    }
    // Validate required fields for update
}

private void validatePatchRequest(UUID id, XxxRequestDto request) {
    if (id == null) {
        throw new XxxException.InvalidRequestException("ID cannot be null");
    }
    if (request == null) {
        throw new XxxException.InvalidRequestException("Request cannot be null");
    }
    
    // At least one field must be provided
    boolean hasAny = request.getField1() != null || request.getField2() != null;
    if (!hasAny) {
        throw new XxxException.InvalidRequestException("At least one field must be provided for update");
    }
}
```

### Business Rules Validation

```java
private void validateBusinessRules(Entity entity, XxxRequestDto request) {
    // Check for duplicates
    validateNoDuplicate(entity, request);
    
    // Validate status transitions
    validateStatusTransition(entity.getStatus(), request.getStatus());
    
    // Validate dependencies
    validateDependencies(request);
}

private void validateNoDuplicate(Entity entity, XxxRequestDto request) {
    boolean duplicateExists = repository
        .findByUniqueFields(request.getField1(), request.getField2())
        .stream()
        .anyMatch(e -> !e.getId().equals(entity.getId()));
        
    if (duplicateExists) {
        throw new XxxException.DuplicateEntityException("Duplicate entity found");
    }
}

private void validateStatusTransition(Status current, Status newStatus) {
    switch (current) {
        case PENDING:
            if (newStatus == Status.ACTIVE || newStatus == Status.CANCELLED) return;
            break;
        case ACTIVE:
            if (newStatus == Status.COMPLETED || newStatus == Status.CANCELLED) return;
            break;
        case COMPLETED:
        case CANCELLED:
            throw new XxxException.InvalidStatusTransitionException(
                "Cannot change status from " + current + " to " + newStatus);
    }
    throw new XxxException.InvalidStatusTransitionException(
        "Invalid status transition from " + current + " to " + newStatus);
}
```

## 5. Business Logic Separation

### Tách logic nghiệp vụ thành methods riêng

```java
private void updateProgressAndCompletion(Entity entity) {
    // Calculate progress
    double progress = calculateProgress(entity);
    entity.setProgress(progress);
    
    // Update completion status
    boolean isCompleted = progress >= 1.0;
    entity.setCompleted(isCompleted);
    
    if (isCompleted && entity.getCompletedAt() == null) {
        entity.setCompletedAt(LocalDateTime.now());
    } else if (!isCompleted) {
        entity.setCompletedAt(null);
    }
}

private double calculateProgress(Entity entity) {
    if (entity.getTotal() > 0) {
        return (double) entity.getCompleted() / entity.getTotal();
    }
    return 0.0;
}

private void applyFullUpdate(Entity entity, XxxRequestDto request) {
    entity.setField1(request.getField1());
    entity.setField2(request.getField2());
    entity.setField3(request.getField3());
}

private void applyPartialUpdate(Entity entity, XxxRequestDto request) {
    if (request.getField1() != null) {
        entity.setField1(request.getField1());
    }
    if (request.getField2() != null) {
        entity.setField2(request.getField2());
    }
    if (request.getField3() != null) {
        entity.setField3(request.getField3());
    }
}
```

## 6. Error Handling

### Exception Hierarchy

```java
public abstract class XxxException extends RuntimeException {
    public XxxException(String message) {
        super(message);
    }
    
    public XxxException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Specific exceptions
    public static class InvalidRequestException extends XxxException {
        public InvalidRequestException(String message) { super(message); }
    }
    
    public static class XxxNotFoundException extends XxxException {
        public XxxNotFoundException(String message) { super(message); }
    }
    
    public static class DuplicateEntityException extends XxxException {
        public DuplicateEntityException(String message) { super(message); }
    }
    
    public static class InvalidStatusTransitionException extends XxxException {
        public InvalidStatusTransitionException(String message) { super(message); }
    }
}
```

### Error Messages

```java
// ✅ Good error messages
throw new XxxException.InvalidRequestException("Enrollment ID cannot be null");
throw new XxxException.XxxNotFoundException("Enrollment not found with ID: " + enrollmentId);
throw new XxxException.DuplicateEntityException("Student is already enrolled in this course");

// ❌ Bad error messages
throw new XxxException.InvalidRequestException("Invalid");
throw new XxxException.XxxNotFoundException("Not found");
```

## 7. Mapping Patterns

### Entity to DTO Mapping

```java
private XxxResponseDto mapToResponse(Entity entity) {
    return XxxResponseDto.builder()
        .id(entity.getId())
        .field1(entity.getField1())
        .field2(entity.getField2())
        .status(entity.getStatus())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
}

private Entity mapToEntity(XxxRequestDto request) {
    return Entity.builder()
        .field1(request.getField1())
        .field2(request.getField2())
        .status(request.getStatus())
        .build();
}
```

## 8. Audit Trail

### Timestamp Management

```java
// Luôn gọi onCreate/onUpdate
entity.onCreate(); // Set createdAt
entity.onUpdate(); // Set updatedAt

// Trong Entity class
public void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
}

public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
}
```

## 9. Dependency Validation

### Kiểm tra dependencies tồn tại

```java
private void validateDependencies(XxxRequestDto request) {
    if (!otherService.existsById(request.getOtherId())) {
        throw new XxxException.InvalidRequestException(
            "Related entity not found with ID: " + request.getOtherId());
    }
}
```

## 10. Naming Conventions

### Methods

- **CRUD**: `createXxx`, `getXxxById`, `updateXxx`, `patchXxx`, `deleteXxx`
- **Validation**: `validateCreateRequest`, `validateBusinessRules`, `validateStatusTransition`
- **Helpers**: `applyFullUpdate`, `applyPartialUpdate`, `updateProgressAndCompletion`
- **Mapping**: `mapToResponse`, `mapToEntity`
- **Business Logic**: `calculateProgress`, `updateCompletionStatus`

### Variables

- **Entities**: `enrollment`, `courseProgress`, `learningProgress`
- **Requests**: `request`, `createRequest`, `updateRequest`
- **Responses**: `response`, `enrollmentResponse`
- **Collections**: `enrollments`, `progressList`

## 11. Best Practices

### Code Quality

1. **Single Responsibility**: Mỗi method có 1 nhiệm vụ rõ ràng
2. **Fail Fast**: Validate input trước khi xử lý
3. **Consistent Error Messages**: Thông báo lỗi rõ ràng, có context
4. **No Side Effects**: Methods pure, không thay đổi state không mong muốn
5. **Immutable Dependencies**: Sử dụng `final` cho dependencies

### Performance

1. **Avoid N+1 Queries**: Sử dụng batch operations khi cần
2. **Lazy Loading**: Chỉ load data cần thiết
3. **Caching**: Cache kết quả tính toán phức tạp
4. **Batch Operations**: Xử lý nhiều records cùng lúc

### Security

1. **Input Sanitization**: Validate và sanitize tất cả input
2. **Authorization**: Kiểm tra quyền truy cập
3. **Audit Logging**: Log các thao tác quan trọng
4. **Data Encryption**: Mã hóa dữ liệu nhạy cảm

## 12. Testing Guidelines

### Unit Tests

```java
@Test
void createXxx_ValidRequest_ReturnsResponse() {
    // Given
    XxxRequestDto request = createValidRequest();
    when(repository.save(any())).thenReturn(mockEntity);
    
    // When
    XxxResponseDto response = service.createXxx(request);
    
    // Then
    assertThat(response).isNotNull();
    assertThat(response.getField()).isEqualTo(request.getField());
    verify(repository).save(any());
}

@Test
void createXxx_NullRequest_ThrowsException() {
    // When & Then
    assertThatThrownBy(() -> service.createXxx(null))
        .isInstanceOf(XxxException.InvalidRequestException.class)
        .hasMessage("Request cannot be null");
}
```

### Integration Tests

```java
@SpringBootTest
@Testcontainers
class XxxServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    
    @Test
    void createXxx_ValidRequest_PersistsToDatabase() {
        // Test với database thật
    }
}
```

## 13. Common Patterns

### Status Management

```java
public enum Status {
    PENDING, ACTIVE, COMPLETED, CANCELLED, SUSPENDED
}

private void validateStatusTransition(Status current, Status newStatus) {
    Map<Status, Set<Status>> allowedTransitions = Map.of(
        Status.PENDING, Set.of(Status.ACTIVE, Status.CANCELLED),
        Status.ACTIVE, Set.of(Status.COMPLETED, Status.CANCELLED, Status.SUSPENDED),
        Status.SUSPENDED, Set.of(Status.ACTIVE, Status.CANCELLED),
        Status.COMPLETED, Set.of(),
        Status.CANCELLED, Set.of()
    );
    
    if (!allowedTransitions.get(current).contains(newStatus)) {
        throw new XxxException.InvalidStatusTransitionException(
            "Cannot transition from " + current + " to " + newStatus);
    }
}
```

### Progress Calculation

```java
private void updateProgress(Entity entity) {
    if (entity.getTotal() > 0) {
        double progress = (double) entity.getCompleted() / entity.getTotal();
        entity.setProgress(Math.round(progress * 100.0) / 100.0); // Round to 2 decimal places
    } else {
        entity.setProgress(0.0);
    }
    
    // Update completion status
    boolean isCompleted = entity.getProgress() >= 1.0;
    entity.setCompleted(isCompleted);
    
    if (isCompleted && entity.getCompletedAt() == null) {
        entity.setCompletedAt(LocalDateTime.now());
    }
}
```

## 14. Checklist

Trước khi commit code, kiểm tra:

- [ ] Tất cả input được validate
- [ ] Business rules được implement đúng
- [ ] Error messages rõ ràng và có context
- [ ] Audit trail được set đúng (onCreate/onUpdate)
- [ ] Dependencies được inject qua constructor
- [ ] Methods được tổ chức theo nhóm chức năng
- [ ] Code được comment đầy đủ
- [ ] Unit tests được viết cho các method chính
- [ ] Exception handling đầy đủ
- [ ] Performance được tối ưu (tránh N+1 queries)

## 15. Examples

Xem các file implementation mẫu:
- `EnrollmentServiceImpl.java` - CRUD operations với business rules
- `CourseProgressServiceImpl.java` - Progress calculation và completion logic
- `LearningProgressServiceImpl.java` - Status management và audit trail

---

**Lưu ý**: Tài liệu này sẽ được cập nhật thường xuyên dựa trên feedback và best practices mới. Mọi thắc mắc vui lòng liên hệ team lead.