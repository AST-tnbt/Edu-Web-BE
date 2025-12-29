# Analytics Service - Event Integration Guide

## Overview
Document này hướng dẫn các service khác cần publish events gì để Analytics Service có thể cập nhật metrics cho Platform Overview.

---

## 🎯 Tổng Quan Kiến Trúc

Analytics Service subscribe các events từ các microservices khác để tracking và tính toán metrics:

```
┌─────────────────┐
│  Auth/User Svc  │──► UserRegisteredEvent ──────┐
│                 │──► UserLoginEvent ───────────┤
└─────────────────┘                               │
                                                  │
┌─────────────────┐                               │
│  Course Service │──► CourseCreatedEvent ────────┤
│                 │──► CoursePublishedEvent ──────┤
└─────────────────┘                               │
                                                  │     ┌──────────────────────┐
┌─────────────────┐                               ├────►│ Analytics Service    │
│ Enrollment Svc  │──► EnrollmentCreatedEvent ────┤     │                      │
└─────────────────┘                               │     │ ✓ UserGrowthAnalytics│
                                                  │     │ ✓ PlatformOverview   │
┌─────────────────┐                               │     │ ✓ InstructorStats    │
│ Payment Service │──► PaymentCompletedEvent ─────┤     └──────────────────────┘
└─────────────────┘                               │
                                                  │
┌─────────────────┐                               │
│ Progress Svc    │──► CourseCompletedEvent ──────┤
│                 │──► CourseProgressUpdatedEvent ┘
└─────────────────┘
```

---

## 📋 Events Cần Implement

### 1. Auth/User Service ✅ (Đã có)

#### UserRegisteredEvent ✅
**Khi nào:** User mới đăng ký thành công

**Exchange:** `auth.exchange`  
**Routing Key:** `user.created`

**Payload:**
```json
{
  "eventId": "uuid",
  "userId": "uuid",
  "email": "string",
  "username": "string",
  "role": "STUDENT|INSTRUCTOR|ADMIN",
  "registeredAt": "2024-01-01T10:00:00",
  "occurredAt": "2024-01-01T10:00:00"
}
```

#### UserLoginEvent ✅
**Khi nào:** User login thành công

**Exchange:** `auth.exchange`  
**Routing Key:** `user.login`

**Payload:**
```json
{
  "eventId": "uuid",
  "userId": "uuid",
  "loginAt": "2024-01-01T10:00:00",
  "occurredAt": "2024-01-01T10:00:00"
}
```

---

### 2. Course Service ❌ (Cần implement)

#### CourseCreatedEvent
**Khi nào:** Instructor tạo course mới (bất kể status)

**Exchange:** `course.exchange`  
**Routing Key:** `course.created`  
**Target Queue:** `analytics.course.created.queue`

**Payload:**
```json
{
  "eventId": "uuid",
  "courseId": "uuid",
  "instructorId": "uuid",
  "title": "string",
  "status": "DRAFT|PUBLISHED",
  "price": 99.99,
  "currency": "USD",
  "createdAt": "2024-01-01T10:00:00",
  "occurredAt": "2024-01-01T10:00:00",
  "version": 1
}
```

**Implementation Example (Spring Boot):**
```java
@Service
public class CourseEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.exchange.course}")
    private String courseExchange;
    
    @Value("${app.rabbitmq.routing-key.course-created}")
    private String courseCreatedRoutingKey;
    
    public void publishCourseCreated(Course course) {
        CourseCreatedEvent event = CourseCreatedEvent.builder()
            .eventId(UUID.randomUUID())
            .courseId(course.getId())
            .instructorId(course.getInstructorId())
            .title(course.getTitle())
            .status(course.getStatus().name())
            .price(course.getPrice())
            .currency("USD")
            .createdAt(course.getCreatedAt())
            .occurredAt(LocalDateTime.now())
            .version(1)
            .build();
            
        rabbitTemplate.convertAndSend(courseExchange, courseCreatedRoutingKey, event);
        log.info("Published CourseCreatedEvent: courseId={}", course.getId());
    }
}
```

#### CoursePublishedEvent
**Khi nào:** Course chuyển từ DRAFT sang PUBLISHED

**Exchange:** `course.exchange`  
**Routing Key:** `course.published`  
**Target Queue:** `analytics.course.published.queue`

**Payload:**
```json
{
  "eventId": "uuid",
  "courseId": "uuid",
  "instructorId": "uuid",
  "title": "string",
  "publishedAt": "2024-01-01T10:00:00",
  "occurredAt": "2024-01-01T10:00:00",
  "version": 1
}
```

---

### 3. Enrollment Service ❌ (Cần implement)

#### EnrollmentCreatedEvent
**Khi nào:** Student enroll vào course thành công

**Exchange:** `enrollment.exchange`  
**Routing Key:** `enrollment.created`  
**Target Queue:** `analytics.enrollment.created.queue`

**Payload:**
```json
{
  "eventId": "uuid",
  "enrollmentId": "uuid",
  "studentId": "uuid",
  "courseId": "uuid",
  "instructorId": "uuid",
  "courseTitle": "string",
  "enrollmentType": "FREE|PAID",
  "enrolledAt": "2024-01-01T10:00:00",
  "occurredAt": "2024-01-01T10:00:00",
  "version": 1
}
```

**Implementation Example:**
```java
@Service
public class EnrollmentEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.exchange.enrollment}")
    private String enrollmentExchange;
    
    @Value("${app.rabbitmq.routing-key.enrollment-created}")
    private String enrollmentCreatedRoutingKey;
    
    public void publishEnrollmentCreated(Enrollment enrollment, Course course) {
        EnrollmentCreatedEvent event = EnrollmentCreatedEvent.builder()
            .eventId(UUID.randomUUID())
            .enrollmentId(enrollment.getId())
            .studentId(enrollment.getStudentId())
            .courseId(enrollment.getCourseId())
            .instructorId(course.getInstructorId())
            .courseTitle(course.getTitle())
            .enrollmentType(enrollment.isFree() ? "FREE" : "PAID")
            .enrolledAt(enrollment.getCreatedAt())
            .occurredAt(LocalDateTime.now())
            .version(1)
            .build();
            
        rabbitTemplate.convertAndSend(enrollmentExchange, enrollmentCreatedRoutingKey, event);
        log.info("Published EnrollmentCreatedEvent: enrollmentId={}", enrollment.getId());
    }
}
```

---

### 4. Payment Service ❌ (Cần implement)

#### PaymentCompletedEvent
**Khi nào:** Payment thành công (status = COMPLETED)

**Exchange:** `payment.exchange`  
**Routing Key:** `payment.completed`  
**Target Queue:** `analytics.payment.completed.queue`

**Payload:**
```json
{
  "eventId": "uuid",
  "paymentId": "uuid",
  "userId": "uuid",
  "courseId": "uuid",
  "instructorId": "uuid",
  "amount": 99.99,
  "currency": "USD",
  "platformFee": 19.99,
  "instructorEarning": 80.00,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "completedAt": "2024-01-01T10:00:00",
  "occurredAt": "2024-01-01T10:00:00",
  "version": 1
}
```

**Implementation Example:**
```java
@Service
public class PaymentEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.exchange.payment}")
    private String paymentExchange;
    
    @Value("${app.rabbitmq.routing-key.payment-completed}")
    private String paymentCompletedRoutingKey;
    
    public void publishPaymentCompleted(Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
            .eventId(UUID.randomUUID())
            .paymentId(payment.getId())
            .userId(payment.getUserId())
            .courseId(payment.getCourseId())
            .instructorId(payment.getInstructorId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .platformFee(payment.getPlatformFee())
            .instructorEarning(payment.getInstructorEarning())
            .paymentMethod(payment.getPaymentMethod())
            .status("COMPLETED")
            .completedAt(payment.getCompletedAt())
            .occurredAt(LocalDateTime.now())
            .version(1)
            .build();
            
        rabbitTemplate.convertAndSend(paymentExchange, paymentCompletedRoutingKey, event);
        log.info("Published PaymentCompletedEvent: paymentId={}, amount={}", 
            payment.getId(), payment.getAmount());
    }
}
```

---

### 5. Progress/Course Service ❌ (Cần implement)

#### CourseCompletedEvent
**Khi nào:** Student hoàn thành course (completion rate = 100%)

**Exchange:** `progress.exchange`  
**Routing Key:** `course.completed`  
**Target Queue:** `analytics.course.completed.queue`

**Payload:**
```json
{
  "eventId": "uuid",
  "studentId": "uuid",
  "courseId": "uuid",
  "instructorId": "uuid",
  "enrollmentId": "uuid",
  "completionRate": 100.0,
  "totalTimeSpentMinutes": 1200,
  "completedAt": "2024-01-01T10:00:00",
  "occurredAt": "2024-01-01T10:00:00",
  "version": 1
}
```

#### CourseProgressUpdatedEvent (Optional - for detailed tracking)
**Khi nào:** Student cập nhật progress (hoàn thành lesson/module)

**Exchange:** `progress.exchange`  
**Routing Key:** `course.progress.updated`  
**Target Queue:** `analytics.course.progress.updated.queue`

**Payload:**
```json
{
  "eventId": "uuid",
  "studentId": "uuid",
  "courseId": "uuid",
  "instructorId": "uuid",
  "enrollmentId": "uuid",
  "previousCompletionRate": 50.0,
  "currentCompletionRate": 75.0,
  "completedLessonId": "uuid",
  "updatedAt": "2024-01-01T10:00:00",
  "occurredAt": "2024-01-01T10:00:00",
  "version": 1
}
```

---

## 🔧 Configuration Required

### RabbitMQ Exchanges
Các service cần tạo/configure các exchanges sau:

```yaml
# Course Service
exchanges:
  - name: course.exchange
    type: topic
    durable: true
    
# Enrollment Service  
exchanges:
  - name: enrollment.exchange
    type: topic
    durable: true

# Payment Service
exchanges:
  - name: payment.exchange
    type: topic
    durable: true
    
# Progress Service
exchanges:
  - name: progress.exchange
    type: topic
    durable: true
```

### Spring Boot Configuration Example

```properties
# application.properties (Course Service)
app.rabbitmq.exchange.course=course.exchange
app.rabbitmq.routing-key.course-created=course.created
app.rabbitmq.routing-key.course-published=course.published

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=user
spring.rabbitmq.password=password
```

---

## 📝 Best Practices

### 1. Event Publishing Pattern
```java
@Service
public class SomeService {
    
    @Transactional
    public void someBusinessLogic() {
        // 1. Business logic
        Entity entity = repository.save(newEntity);
        
        // 2. Publish event (within transaction)
        eventPublisher.publish(createEvent(entity));
    }
}
```

### 2. Error Handling
- Events phải **idempotent** (có thể process nhiều lần)
- Include `eventId` để track duplicates
- Include `occurredAt` timestamp
- Include `version` để support event schema evolution

### 3. Event Versioning
```java
// Version 1
{
  "eventId": "uuid",
  "version": 1,
  "data": {...}
}

// Version 2 (backward compatible)
{
  "eventId": "uuid",
  "version": 2,
  "data": {...},
  "newField": "value"  // New optional field
}
```

### 4. Testing
Test event publishing trong integration tests:

```java
@Test
void shouldPublishCourseCreatedEvent() {
    // Given
    Course course = createCourse();
    
    // When
    courseService.createCourse(course);
    
    // Then
    verify(rabbitTemplate).convertAndSend(
        eq("course.exchange"),
        eq("course.created"),
        argThat(event -> 
            event.getCourseId().equals(course.getId())
        )
    );
}
```

---

## 🚀 Rollout Plan

### Phase 1: User Events ✅
- UserRegisteredEvent - **Đã implement**
- UserLoginEvent - **Đã implement**

### Phase 2: Course & Enrollment (Priority)
1. Course Service implement CourseCreatedEvent và CoursePublishedEvent
2. Enrollment Service implement EnrollmentCreatedEvent
3. Test integration với Analytics Service

### Phase 3: Revenue Tracking
1. Payment Service implement PaymentCompletedEvent
2. Test revenue calculation

### Phase 4: Engagement Metrics
1. Progress Service implement CourseCompletedEvent
2. Implement CourseProgressUpdatedEvent (optional)

---

## 🧪 Testing Checklist

### For Each Service:
- [ ] Event được publish sau khi transaction commit
- [ ] Event payload contains all required fields
- [ ] Event có thể serialize/deserialize thành công
- [ ] RabbitMQ exchange và routing key đúng
- [ ] Handle publish failures gracefully
- [ ] Log event publishing để debugging
- [ ] Integration test với Analytics Service

### Integration Testing:
```bash
# 1. Start RabbitMQ
docker-compose up -d rabbitmq

# 2. Start Analytics Service
cd analysticservice && mvn spring-boot:run

# 3. Start your service
cd yourservice && mvn spring-boot:run

# 4. Trigger business flow
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d '{"title": "Test Course", ...}'

# 5. Check Analytics Service logs
# Should see: "Received CourseCreatedEvent: courseId=..."
```

---

## 📞 Contact & Support

**Analytics Service Team:**
- Repository: `/analysticservice`
- Questions: Tạo issue hoặc contact team

**Event DTOs Location:**
```
analysticservice/src/main/java/com/se347/analysticservice/dtos/events/
├── course/
│   ├── CourseCreatedEvent.java
│   └── CoursePublishedEvent.java
├── enrollment/
│   └── EnrollmentCreatedEvent.java
├── payment/
│   └── PaymentCompletedEvent.java
└── progress/
    ├── CourseCompletedEvent.java
    └── CourseProgressUpdatedEvent.java
```

**Listener Location:**
```
analysticservice/src/main/java/com/se347/analysticservice/listeners/
├── CourseEventListener.java
├── EnrollmentEventListener.java
├── PaymentEventListener.java
├── ProgressEventListener.java
└── UserEventListener.java (✅ active)
```

---

## 📚 References

- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP](https://spring.io/projects/spring-amqp)
- [Event-Driven Architecture Best Practices](https://microservices.io/patterns/data/event-driven-architecture.html)

