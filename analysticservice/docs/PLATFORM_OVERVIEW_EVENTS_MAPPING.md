# Platform Overview - Events Mapping

## Overview
Document này mô tả các events cần thiết từ các microservices để cập nhật `PlatformOverview` aggregate.

---

## 📊 Metrics & Required Events

### 1️⃣ **User Metrics** (từ Auth/User Service)

| Metric | Event Cần Thiết | Service | Data Cần Có |
|--------|----------------|---------|-------------|
| `totalUsers` | `UserRegisteredEvent` | User Service | userId, role, registeredAt |
| `totalUsers` | `UserDeletedEvent` ⚠️ | User Service | userId, role, deletedAt |
| `newUsersCount` | `UserRegisteredEvent` | User Service | userId, role, registeredAt |
| `userGrowthRate` | Tính từ newUsersCount | - | - |

**Events đã có:**
```java
✅ UserRegisteredEvent - Đã implement
❌ UserDeletedEvent - Chưa có (nếu có tính năng xóa user)
```

---

### 2️⃣ **Course Metrics** (từ Course Service)

| Metric | Event Cần Thiết | Service | Data Cần Có |
|--------|----------------|---------|-------------|
| `totalActiveCourses` | `CoursePublishedEvent` | Course Service | courseId, publishedAt, status |
| `totalActiveCourses` | `CourseUnpublishedEvent` | Course Service | courseId, unpublishedAt, status |
| `totalActiveCourses` | `CourseArchivedEvent` | Course Service | courseId, archivedAt |
| `newCoursesCount` | `CourseCreatedEvent` | Course Service | courseId, createdAt |
| `newCoursesCount` | `CoursePublishedEvent` | Course Service | courseId, publishedAt |

**Events cần tạo:**
```java
❌ CourseCreatedEvent - Khi instructor tạo course mới
❌ CoursePublishedEvent - Khi course chuyển sang PUBLISHED
❌ CourseUnpublishedEvent - Khi course bị unpublish
❌ CourseArchivedEvent - Khi course bị archive/delete
```

---

### 3️⃣ **Enrollment Metrics** (từ Enrollment/Course Service)

| Metric | Event Cần Thiết | Service | Data Cần Có |
|--------|----------------|---------|-------------|
| `totalEnrollments` | `EnrollmentCreatedEvent` | Enrollment Service | enrollmentId, userId, courseId, enrolledAt |
| `totalEnrollments` | `EnrollmentCancelledEvent` | Enrollment Service | enrollmentId, cancelledAt |
| `newEnrollmentsCount` | `EnrollmentCreatedEvent` | Enrollment Service | enrollmentId, enrolledAt |
| `enrollmentGrowthRate` | Tính từ newEnrollmentsCount | - | - |

**Events cần tạo:**
```java
❌ EnrollmentCreatedEvent - Khi user enroll vào course
❌ EnrollmentCancelledEvent - Khi user hủy enrollment (nếu có)
```

---

### 4️⃣ **Revenue Metrics** (từ Payment/Transaction Service)

| Metric | Event Cần Thiết | Service | Data Cần Có |
|--------|----------------|---------|-------------|
| `totalRevenue` | `PaymentCompletedEvent` | Payment Service | paymentId, amount, currency, completedAt |
| `totalRevenue` | `RefundProcessedEvent` | Payment Service | refundId, amount, currency, refundedAt |
| `revenueGrowthRate` | Tính từ period revenue | - | - |

**Events cần tạo:**
```java
❌ PaymentCompletedEvent - Khi payment thành công
❌ RefundProcessedEvent - Khi refund được xử lý
```

---

### 5️⃣ **Course Completion Metrics** (từ Course/Progress Service)

| Metric | Event Cần Thiết | Service | Data Cần Có |
|--------|----------------|---------|-------------|
| `averageCompletionRate` | `CourseCompletionUpdatedEvent` | Progress Service | userId, courseId, completionRate, updatedAt |
| `averageCompletionRate` | `CourseCompletedEvent` | Progress Service | userId, courseId, completedAt |

**Events cần tạo:**
```java
❌ CourseCompletionUpdatedEvent - Khi progress thay đổi
❌ CourseCompletedEvent - Khi user hoàn thành course (100%)
```

---

## 🏗️ Event DTOs Structure

### Template cho External Events

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class <EventName> {
    
    // Event metadata
    private UUID eventId;           // Unique event identifier
    private LocalDateTime occurredAt; // When event occurred
    
    // Business data
    private UUID <entityId>;        // Entity ID (userId, courseId, etc.)
    private <Type> <businessData>;  // Business-specific data
    
    // Optional: Aggregate info
    private String aggregateType;   // e.g., "Course", "User"
    private Integer version;        // Event version for compatibility
}
```

---

## 📋 Implementation Checklist

### Phase 1: User Metrics ✅
- [✅] UserRegisteredEvent - Đã có
- [✅] UserGrowthAnalytics listener - Đã có
- [ ] UserDeletedEvent (optional)

### Phase 2: Course Metrics
- [ ] CourseCreatedEvent
- [ ] CoursePublishedEvent
- [ ] CourseUnpublishedEvent
- [ ] CourseArchivedEvent

### Phase 3: Enrollment Metrics
- [ ] EnrollmentCreatedEvent
- [ ] EnrollmentCancelledEvent (optional)

### Phase 4: Revenue Metrics
- [ ] PaymentCompletedEvent
- [ ] RefundProcessedEvent (optional)

### Phase 5: Completion Metrics
- [ ] CourseCompletionUpdatedEvent
- [ ] CourseCompletedEvent

### Phase 6: Platform Overview Service
- [ ] PlatformOverviewService interface
- [ ] PlatformOverviewServiceImpl
- [ ] PlatformOverviewEventListener (subscribe to all events)
- [ ] PlatformAnalyticsScheduler (batch job)

---

## 🔄 Event Flow Architecture

```
┌─────────────────┐
│  User Service   │──► UserRegisteredEvent ──────┐
└─────────────────┘                               │
                                                  │
┌─────────────────┐                               │
│ Course Service  │──► CourseCreatedEvent ────────┤
│                 │──► CoursePublishedEvent ──────┤
└─────────────────┘                               │
                                                  │
┌─────────────────┐                               ├──► AnalyticService
│Enrollment Svc   │──► EnrollmentCreatedEvent ────┤    ┌──────────────────┐
└─────────────────┘                               │    │ Event Listeners  │
                                                  │    │  ↓               │
┌─────────────────┐                               │    │ Update Metrics   │
│ Payment Service │──► PaymentCompletedEvent ─────┤    │  ↓               │
└─────────────────┘                               │    │ PlatformOverview │
                                                  │    └──────────────────┘
┌─────────────────┐                               │
│ Progress Service│──► CourseCompletedEvent ──────┘
└─────────────────┘
```

---

## 🎯 Implementation Strategy

### Option 1: Real-time Event-Driven (Phức tạp)
**Pros:**
- Real-time updates
- Reactive architecture
- Granular tracking

**Cons:**
- Nhiều events cần handle
- Performance overhead
- Phức tạp debug

### Option 2: Batch + Event Hybrid (Khuyến nghị) ✅
**Pros:**
- Balance giữa real-time và batch
- Performance tốt
- Dễ maintain

**Cons:**
- Không 100% real-time
- Cần scheduled job

**Implementation:**
1. Subscribe events quan trọng (User, Enrollment) → real-time
2. Aggregate data từ DB cho metrics khác → batch job
3. Scheduled job chạy mỗi ngày/tuần/tháng để tính growth rates

### Option 3: Pure Batch (Đơn giản nhất)
**Pros:**
- Rất đơn giản
- Performance tốt
- Dễ implement

**Cons:**
- Không real-time
- Phụ thuộc vào schedule

---

## 🛠️ Next Steps

1. **Định nghĩa events với các service teams**
   - Meeting với Course Service team
   - Meeting với Payment Service team
   - Meeting với Progress Service team

2. **Tạo Event DTOs trong AnalyticService**
   ```
   src/main/java/com/se347/analysticservice/dtos/events/
   ├── course/
   │   ├── CourseCreatedEvent.java
   │   ├── CoursePublishedEvent.java
   │   └── ...
   ├── enrollment/
   │   └── EnrollmentCreatedEvent.java
   ├── payment/
   │   └── PaymentCompletedEvent.java
   └── progress/
       └── CourseCompletedEvent.java
   ```

3. **Implement Listeners**
   ```
   src/main/java/com/se347/analysticservice/listeners/
   ├── UserEventListener.java (✅ Đã có)
   ├── CourseEventListener.java
   ├── EnrollmentEventListener.java
   ├── PaymentEventListener.java
   └── ProgressEventListener.java
   ```

4. **Implement Services**
   - PlatformOverviewService
   - PlatformMetricsAggregationService

5. **Configure RabbitMQ queues** trong application.properties

6. **Testing**
   - Unit tests cho listeners
   - Integration tests cho event flow
   - End-to-end tests

---

## 📝 Notes

- Events phải **idempotent** (có thể process nhiều lần without side effects)
- Cần **error handling** và **retry mechanism**
- Cần **dead letter queue** cho failed events
- Cần **event versioning** để backward compatibility
- Xem xét **event sourcing** nếu cần audit trail đầy đủ

