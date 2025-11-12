# Đề xuất cải thiện Logic tạo Learning Progress

## ✅ Logic hiện tại - Lazy Initialization Pattern

**Thiết kế hiện tại là ĐÚNG và hợp lý:**

1. **Khi tạo Enrollment** (trong `PaymentListenerImpl`):
   - ✅ Tạo `Enrollment` record
   - ✅ Tạo `CourseProgress` record (với `totalLessons` và `lessonsCompleted = 0`)
   - ❌ **KHÔNG** tạo `LearningProgress` cho tất cả lessons

2. **Khi user truy cập lesson lần đầu** (trong `getLearningProgressByLessonIdAndEnrollmentId`):
   - ✅ Tự động tạo `LearningProgress` cho lesson đó (lazy initialization)

**Lợi ích của pattern này:**
- 💾 **Tiết kiệm storage**: Không tạo records không cần thiết (nhiều user không học hết course)
- ⚡ **Performance tốt**: Tạo enrollment nhanh hơn (không phải tạo hàng trăm learning progress records)
- 🎯 **On-demand**: Chỉ tạo khi user thực sự truy cập lesson
- 📊 **Scalability**: Với course có 100+ lessons, tiết kiệm rất nhiều storage

**Ví dụ:**
- Course có 50 lessons
- 1000 users enroll
- Nếu tạo tất cả: 50,000 learning progress records
- Với lazy init: Chỉ tạo khi user truy cập (có thể chỉ 10,000-20,000 records)

---

## 🔍 Vấn đề cần cải thiện

### 1. **Thiếu validation Enrollment Status & Payment Status**

**Code hiện tại** (`getLearningProgressByLessonIdAndEnrollmentId`):
```java
public LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId) {
    LearningProgress learningProgress = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
    if (learningProgress == null) {
        createLearningProgress(...); // ❌ Không kiểm tra enrollment status/payment status
    }
    return mapToResponse(learningProgress);
}
```

**Vấn đề**: 
- User có thể truy cập lesson ngay cả khi enrollment status = `CANCELLED` hoặc `SUSPENDED`
- User có thể truy cập lesson khi payment status = `PENDING` hoặc `REFUNDED`
- Không kiểm tra `access_expires_at`

### 2. **Thiếu validation Lesson thuộc Course**

**Vấn đề**: Không kiểm tra lesson có thuộc course của enrollment không. User có thể tạo learning progress cho lesson không liên quan.

### 3. **Race Condition**

**Vấn đề**: Nếu 2 requests cùng lúc truy cập lesson lần đầu, có thể xảy ra:
- Request 1: Không tìm thấy → tạo mới
- Request 2: Không tìm thấy → tạo mới (trước khi Request 1 commit)
- Kết quả: Có thể tạo duplicate (mặc dù có unique constraint, nhưng vẫn có thể fail)

### 4. **Logic không nhất quán**

- `createLearningProgress()` throw exception nếu duplicate
- `getLearningProgressByLessonIdAndEnrollmentId()` lại tự tạo nếu không tìm thấy

---

## ✅ Đề xuất cải thiện

### Solution 1: Thêm validation trước khi tạo Learning Progress

```java
@Transactional
@Override
public LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId) {
    // 1. Kiểm tra và validate enrollment
    EnrollmentResponseDto enrollment = validateEnrollmentForAccess(enrollmentId);
    
    // 2. Validate lesson thuộc course (nếu có CourseService client)
    validateLessonBelongsToCourse(lessonId, enrollment.getCourseId());
    
    // 3. Get or create với retry logic để tránh race condition
    LearningProgress learningProgress = getOrCreateLearningProgress(lessonId, enrollmentId);
    
    // 4. Update last accessed time
    learningProgress.setLastAccessedAt(LocalDateTime.now());
    learningProgressRepository.save(learningProgress);
    
    return mapToResponse(learningProgress);
}

/**
 * Validate enrollment có thể truy cập lesson không
 */
private EnrollmentResponseDto validateEnrollmentForAccess(UUID enrollmentId) {
    EnrollmentResponseDto enrollment = enrollmentService.getEnrollmentById(enrollmentId);
    
    // Kiểm tra enrollment status
    if (enrollment.getEnrollmentStatus() != EnrollmentStatus.ACTIVE) {
        throw new EnrollmentException.InvalidRequestException(
            "Cannot access lesson. Enrollment status is: " + enrollment.getEnrollmentStatus());
    }
    
    // Kiểm tra payment status
    if (enrollment.getPaymentStatus() != PaymentStatus.PAID) {
        throw new EnrollmentException.PaymentRequiredException(
            "Payment required. Current payment status: " + enrollment.getPaymentStatus());
    }
    
    // Kiểm tra access expiration (nếu có)
    if (enrollment.getAccessExpiresAt() != null && 
        enrollment.getAccessExpiresAt().isBefore(LocalDateTime.now())) {
        throw new EnrollmentException.EnrollmentExpiredException(enrollmentId.toString());
    }
    
    return enrollment;
}

/**
 * Validate lesson thuộc course (cần integrate với CourseService)
 */
private void validateLessonBelongsToCourse(UUID lessonId, UUID courseId) {
    // TODO: Gọi CourseService để kiểm tra lesson có thuộc course không
    // boolean belongsToCourse = courseServiceClient.isLessonInCourse(lessonId, courseId);
    // if (!belongsToCourse) {
    //     throw new LearningProgressException.InvalidRequestException(
    //         "Lesson " + lessonId + " does not belong to course " + courseId);
    // }
}

/**
 * Get or create với retry logic để tránh race condition
 */
private LearningProgress getOrCreateLearningProgress(UUID lessonId, UUID enrollmentId) {
    // Thử tìm trước
    LearningProgress existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
    if (existing != null) {
        return existing;
    }
    
    // Nếu không tìm thấy, thử tạo mới với retry
    int maxRetries = 3;
    for (int i = 0; i < maxRetries; i++) {
        try {
            LearningProgress newProgress = LearningProgress.builder()
                .enrollmentId(enrollmentId)
                .lessonId(lessonId)
                .isCompleted(false)
                .lastAccessedAt(LocalDateTime.now())
                .build();
            
            return learningProgressRepository.save(newProgress);
        } catch (DataIntegrityViolationException e) {
            // Duplicate key - có thể do race condition, thử lại
            if (i < maxRetries - 1) {
                // Wait một chút trước khi retry
                try {
                    Thread.sleep(50 * (i + 1)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LearningProgressException.InvalidRequestException("Interrupted during retry");
                }
                // Thử tìm lại
                existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
                if (existing != null) {
                    return existing;
                }
            } else {
                // Lần cuối, thử tìm lại
                existing = learningProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollmentId);
                if (existing != null) {
                    return existing;
                }
                throw new LearningProgressException.InvalidRequestException(
                    "Failed to create learning progress after retries: " + e.getMessage());
            }
        }
    }
    
    throw new LearningProgressException.InvalidRequestException("Failed to create learning progress");
}
```

### Solution 2: Sử dụng Database-level locking (Pessimistic Lock)

```java
@Transactional
@Override
public LearningProgressResponseDto getLearningProgressByLessonIdAndEnrollmentId(UUID lessonId, UUID enrollmentId) {
    // Validate enrollment
    validateEnrollmentForAccess(enrollmentId);
    
    // Sử dụng pessimistic lock để tránh race condition
    LearningProgress learningProgress = learningProgressRepository
        .findByLessonIdAndEnrollmentIdWithLock(lessonId, enrollmentId);
    
    if (learningProgress == null) {
        learningProgress = createLearningProgressSafely(lessonId, enrollmentId);
    } else {
        // Update last accessed
        learningProgress.setLastAccessedAt(LocalDateTime.now());
        learningProgressRepository.save(learningProgress);
    }
    
    return mapToResponse(learningProgress);
}

// Trong Repository, thêm method với lock:
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT lp FROM LearningProgress lp WHERE lp.lessonId = :lessonId AND lp.enrollmentId = :enrollmentId")
LearningProgress findByLessonIdAndEnrollmentIdWithLock(@Param("lessonId") UUID lessonId, @Param("enrollmentId") UUID enrollmentId);
```

### Solution 3: Sử dụng Unique Index với ON CONFLICT (PostgreSQL) hoặc INSERT IGNORE

Nếu dùng PostgreSQL, có thể dùng:
```sql
INSERT INTO learning_progress (enrollment_id, lesson_id, is_completed, last_accessed_at)
VALUES (?, ?, false, NOW())
ON CONFLICT (lesson_id, enrollment_id) 
DO UPDATE SET last_accessed_at = NOW()
RETURNING *;
```

---

## 📋 Checklist cải thiện

- [ ] ✅ Thêm validation enrollment status = ACTIVE
- [ ] ✅ Thêm validation payment status = PAID  
- [ ] ✅ Thêm validation access expiration
- [ ] ✅ Thêm validation lesson thuộc course (nếu có CourseService)
- [ ] ✅ Xử lý race condition (retry logic hoặc pessimistic lock)
- [ ] ✅ Update `lastAccessedAt` mỗi lần truy cập
- [ ] ✅ Thêm logging để track access patterns
- [ ] ✅ Thêm unit tests cho các edge cases

---

## 🎯 Kết luận

**Logic hiện tại**: ⚠️ **Chưa đủ an toàn**

**Cần cải thiện**:
1. **Bắt buộc**: Thêm validation enrollment status và payment status
2. **Quan trọng**: Xử lý race condition
3. **Nên có**: Validate lesson thuộc course
4. **Tốt để có**: Logging và monitoring

**Độ ưu tiên**:
1. 🔴 **Cao**: Validation enrollment/payment status
2. 🟡 **Trung bình**: Race condition handling
3. 🟢 **Thấp**: Lesson validation (có thể làm sau)

