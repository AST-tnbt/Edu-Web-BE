# Analytics Service - Event-Driven Architecture Design

## **Tổng quan Kiến trúc**

```
┌─────────────────────────────────────────────────────────────────────┐
│                         EduWeb Microservices                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │ UserService  │  │CourseService │  │PaymentService│             │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘             │
│         │                  │                  │                     │
│         │ Events           │ Events           │ Events              │
│         ▼                  ▼                  ▼                     │
│  ┌─────────────────────────────────────────────────────┐          │
│  │            Message Broker (RabbitMQ/Kafka)          │          │
│  └─────────────────────────┬───────────────────────────┘          │
│                            │ Listen Events                         │
│                            ▼                                       │
│  ┌─────────────────────────────────────────────────────┐          │
│  │            AnalyticsService (Event Listener)        │          │
│  │  - Process events                                   │          │
│  │  - Aggregate data                                   │          │
│  │  - Store analytics                                  │          │
│  └─────────────────────────────────────────────────────┘          │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## **1. EVENT FLOWS - DailyRevenue & TransactionSnapshot**

### **Nguồn: PaymentService / EnrollmentService**

```
┌─────────────────────────────────────────────────────────────────────┐
│                         DAILY REVENUE FLOW                          │
└─────────────────────────────────────────────────────────────────────┘

PaymentService                    AnalyticsService
     │                                   │
     │  Student mua course               │
     ├──────────────────────┐            │
     │ Process Payment      │            │
     │ Create Transaction   │            │
     └──────────────────────┘            │
     │                                   │
     │ Publish Event:                    │
     │ "TransactionCompletedEvent"       │
     ├──────────────────────────────────>│
     │                                   │
     │                                   ├─────────────────────┐
     │                                   │ Event Handler       │
     │                                   │ 1. Find/Create      │
     │                                   │    DailyRevenue     │
     │                                   │ 2. Create           │
     │                                   │    TransactionSnapshot│
     │                                   │ 3. Update totals    │
     │                                   │ 4. Save             │
     │                                   └─────────────────────┘
     │                                   │
     │                              [Analytics Stored]
     │                                   │
```

### **Event Schema - TransactionCompletedEvent**

```json
{
  "eventId": "uuid",
  "eventType": "TransactionCompletedEvent",
  "occurredAt": "2025-01-15T10:30:00Z",
  "payload": {
    "transactionId": "uuid",
    "enrollmentId": "uuid",
    "courseId": "uuid",
    "studentId": "uuid",
    "instructorId": "uuid",
    "amount": 299.99,
    "currency": "USD",
    "timestamp": "2025-01-15T10:30:00Z"
  }
}
```

### **Handler Logic - TransactionCompletedEventHandler**

```java
@EventListener
public void handleTransactionCompleted(TransactionCompletedEvent event) {
    LocalDate today = LocalDate.now();
    
    // 1. Find or create DailyRevenue for today
    DailyRevenue dailyRevenue = dailyRevenueRepository
        .findByDate(today)
        .orElseGet(() -> DailyRevenue.create(
            today, 
            Money.zero(), 
            Count.zero()
        ));
    
    // 2. Create TransactionSnapshot
    TransactionSnapshot snapshot = TransactionSnapshot.create(
        dailyRevenue,
        event.getTransactionId(),
        event.getEnrollmentId(),
        event.getCourseId(),
        event.getStudentId(),
        event.getInstructorId(),
        Money.of(event.getAmount()),
        event.getTimestamp()
    );
    
    // 3. Update DailyRevenue totals
    Money newTotal = dailyRevenue.getTotalRevenue()
        .add(Money.of(event.getAmount()));
    Count newCount = dailyRevenue.getTotalTransactions()
        .increment();
    
    dailyRevenue.updateMetrics(newTotal, newCount);
    dailyRevenue.getTransactions().add(snapshot);
    
    // 4. Save
    dailyRevenueRepository.save(dailyRevenue);
}
```

---

## **2. EVENT FLOWS - InstructorRevenue & CourseRevenueSnapshot**

### **Nguồn: PaymentService + Scheduled Job**

```
┌─────────────────────────────────────────────────────────────────────┐
│                      INSTRUCTOR REVENUE FLOW                        │
└─────────────────────────────────────────────────────────────────────┘

Scheduled Job                     AnalyticsService
(Daily/Weekly/Monthly)                   │
     │                                   │
     │ Trigger: End of Period            │
     ├──────────────────────┐            │
     │ Calculate Instructor  │            │
     │ Revenue for Period    │            │
     └──────────────────────┘            │
     │                                   │
     ├───────────────────────────────────>│
     │  Query TransactionSnapshot         │
     │  for period & instructor           │
     │                                   │
     │                                   ├─────────────────────┐
     │                                   │ 1. Aggregate        │
     │                                   │    transactions by  │
     │                                   │    instructor       │
     │                                   │ 2. Calculate totals │
     │                                   │ 3. Find top courses │
     │                                   │ 4. Create           │
     │                                   │    InstructorRevenue│
     │                                   │ 5. Save             │
     │                                   └─────────────────────┘
     │                                   │
     │                          [Revenue Calculated]
     │                                   │
     │<──────────────────────────────────┤
     │  Publish:                         │
     │  InstructorRevenueCalculatedEvent │
     │                                   │
```

### **Alternative: Event-based Trigger**

```
PaymentService                    AnalyticsService
     │                                   │
     │ End of Period                     │
     │ Publish:                          │
     │ "PeriodEndedEvent"                │
     ├──────────────────────────────────>│
     │                                   │
     │                                   ├─────────────────────┐
     │                                   │ Calculate Revenue   │
     │                                   │ for all instructors │
     │                                   │ in that period      │
     │                                   └─────────────────────┘
```

### **Event Schema - PeriodEndedEvent**

```json
{
  "eventId": "uuid",
  "eventType": "PeriodEndedEvent",
  "occurredAt": "2025-01-31T23:59:59Z",
  "payload": {
    "period": "MONTHLY",
    "startDate": "2025-01-01",
    "endDate": "2025-01-31"
  }
}
```

---

## **3. EVENT FLOWS - UserGrowthAnalytics**

### **Nguồn: UserService**

```
┌─────────────────────────────────────────────────────────────────────┐
│                      USER GROWTH ANALYTICS FLOW                     │
└─────────────────────────────────────────────────────────────────────┘

UserService                       AnalyticsService
     │                                   │
     │ User Registration                 │
     │ Publish: "UserRegisteredEvent"    │
     ├──────────────────────────────────>│
     │                                   ├─────────────────────┐
     │                                   │ Update:             │
     │                                   │ - newUsersCount++   │
     │                                   │ - totalUsers++      │
     │                                   └─────────────────────┘
     │                                   │
     │ User Login                        │
     │ Publish: "UserLoggedInEvent"      │
     ├──────────────────────────────────>│
     │                                   ├─────────────────────┐
     │                                   │ Update:             │
     │                                   │ - activeUsersCount++│
     │                                   │ (for today)         │
     │                                   └─────────────────────┘
     │                                   │
     │ Daily Scheduled Job               │
     │ Publish: "DailyUserStatsEvent"    │
     ├──────────────────────────────────>│
     │                                   ├─────────────────────┐
     │                                   │ Calculate:          │
     │                                   │ - Retention rate    │
     │                                   │ - Growth rate       │
     │                                   │ Create/Update       │
     │                                   │ UserGrowthAnalytics │
     │                                   └─────────────────────┘
```

### **Event Schemas**

**UserRegisteredEvent:**
```json
{
  "eventId": "uuid",
  "eventType": "UserRegisteredEvent",
  "occurredAt": "2025-01-15T10:30:00Z",
  "payload": {
    "userId": "uuid",
    "email": "user@example.com",
    "role": "STUDENT",  // or "INSTRUCTOR"
    "registeredAt": "2025-01-15T10:30:00Z"
  }
}
```

**UserLoggedInEvent:**
```json
{
  "eventId": "uuid",
  "eventType": "UserLoggedInEvent",
  "occurredAt": "2025-01-15T14:20:00Z",
  "payload": {
    "userId": "uuid",
    "timestamp": "2025-01-15T14:20:00Z"
  }
}
```

**DailyUserStatsEvent** (từ UserService scheduled job):
```json
{
  "eventId": "uuid",
  "eventType": "DailyUserStatsEvent",
  "occurredAt": "2025-01-15T23:59:59Z",
  "payload": {
    "date": "2025-01-15",
    "totalUsers": 10000,
    "activeUsers": 2500,
    "newUsers": 100
  }
}
```

---

## **4. EVENT FLOWS - InstructorStats**

### **Nguồn: CourseService + EnrollmentService**

```
┌─────────────────────────────────────────────────────────────────────┐
│                      INSTRUCTOR STATS FLOW                          │
└─────────────────────────────────────────────────────────────────────┘

CourseService                     AnalyticsService
     │                                   │
     │ Course Published                  │
     │ Publish: "CoursePublishedEvent"   │
     ├──────────────────────────────────>│
     │                                   ├─────────────────────┐
     │                                   │ Update:             │
     │                                   │ InstructorStats     │
     │                                   │ totalCourses++      │
     │                                   └─────────────────────┘
     │                                   │

EnrollmentService                 AnalyticsService
     │                                   │
     │ Student Enrolled                  │
     │ Publish: "StudentEnrolledEvent"   │
     ├──────────────────────────────────>│
     │                                   ├─────────────────────┐
     │                                   │ Update:             │
     │                                   │ InstructorStats     │
     │                                   │ totalStudents++     │
     │                                   └─────────────────────┘
```

### **Event Schemas**

**CoursePublishedEvent:**
```json
{
  "eventId": "uuid",
  "eventType": "CoursePublishedEvent",
  "occurredAt": "2025-01-15T10:00:00Z",
  "payload": {
    "courseId": "uuid",
    "instructorId": "uuid",
    "title": "Advanced Java Programming",
    "publishedAt": "2025-01-15T10:00:00Z"
  }
}
```

**StudentEnrolledEvent:**
```json
{
  "eventId": "uuid",
  "eventType": "StudentEnrolledEvent",
  "occurredAt": "2025-01-15T14:30:00Z",
  "payload": {
    "enrollmentId": "uuid",
    "courseId": "uuid",
    "studentId": "uuid",
    "instructorId": "uuid",
    "enrolledAt": "2025-01-15T14:30:00Z"
  }
}
```

---

## **5. EVENT FLOWS - PlatformOverview**

### **Nguồn: Scheduled Aggregation Job**

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PLATFORM OVERVIEW FLOW                         │
└─────────────────────────────────────────────────────────────────────┘

Scheduled Job                     AnalyticsService
(Daily/Weekly/Monthly)                   │
     │                                   │
     │ Trigger: End of Period            │
     ├───────────────────────────────────>│
     │                                   │
     │                                   ├─────────────────────┐
     │                                   │ Aggregate from:     │
     │                                   │ 1. UserGrowthAnalytics│
     │                                   │ 2. DailyRevenue     │
     │                                   │ 3. InstructorStats  │
     │                                   │ 4. External queries │
     │                                   │                     │
     │                                   │ Calculate:          │
     │                                   │ - Total metrics     │
     │                                   │ - Growth rates      │
     │                                   │ - Comparisons       │
     │                                   │                     │
     │                                   │ Create/Update       │
     │                                   │ PlatformOverview    │
     │                                   └─────────────────────┘
     │                                   │
     │                          [Overview Created]
```

**Note:** PlatformOverview không listen direct events, mà aggregate từ data đã có trong analytics DB.

---

## **6. COMPLETE EVENT MAPPING**

### **Analytics Entities ← External Events**

| Analytics Entity | Listens To | Source Service | Event Type |
|-----------------|------------|----------------|------------|
| **DailyRevenue** | Transaction events | PaymentService | `TransactionCompletedEvent` |
| **TransactionSnapshot** | Transaction events | PaymentService | `TransactionCompletedEvent` |
| **InstructorRevenue** | Period ended | Scheduled Job | `PeriodEndedEvent` (internal) |
| **CourseRevenueSnapshot** | Period ended | Scheduled Job | `PeriodEndedEvent` (internal) |
| **UserGrowthAnalytics** | User events | UserService | `UserRegisteredEvent`<br>`UserLoggedInEvent`<br>`DailyUserStatsEvent` |
| **InstructorStats** | Course & Enrollment | CourseService<br>EnrollmentService | `CoursePublishedEvent`<br>`StudentEnrolledEvent` |
| **PlatformOverview** | Internal aggregation | Scheduled Job | Aggregate from existing data |

---

## **7. EVENT HANDLERS ARCHITECTURE**

```
AnalyticsService
│
├── listeners/
│   ├── payment/
│   │   └── TransactionEventListener.java
│   │       └── handleTransactionCompleted()
│   │
│   ├── user/
│   │   └── UserEventListener.java
│   │       ├── handleUserRegistered()
│   │       ├── handleUserLoggedIn()
│   │       └── handleDailyUserStats()
│   │
│   ├── course/
│   │   └── CourseEventListener.java
│   │       ├── handleCoursePublished()
│   │       └── handleCourseUnpublished()
│   │
│   └── enrollment/
│       └── EnrollmentEventListener.java
│           └── handleStudentEnrolled()
│
├── jobs/
│   ├── InstructorRevenueCalculationJob.java
│   │   └── @Scheduled(cron = "0 0 1 1 * ?")  // Monthly
│   │
│   └── PlatformOverviewAggregationJob.java
│       └── @Scheduled(cron = "0 0 2 * * ?")  // Daily
│
└── services/
    ├── DailyRevenueService.java
    ├── InstructorRevenueService.java
    ├── UserGrowthService.java
    └── InstructorStatsService.java
```

---

## **8. MESSAGE BROKER CONFIGURATION**

### **RabbitMQ Exchange & Queue Setup**

```
┌─────────────────────────────────────────────────────────────────┐
│                         RabbitMQ Setup                          │
└─────────────────────────────────────────────────────────────────┘

Exchanges:
  - payment.events (topic)
  - user.events (topic)
  - course.events (topic)
  - enrollment.events (topic)

Queues (for AnalyticsService):
  - analytics.payment.queue
    └── Binding: payment.events / transaction.completed
  
  - analytics.user.queue
    └── Binding: user.events / user.registered
                               user.loggedin
                               user.daily.stats
  
  - analytics.course.queue
    └── Binding: course.events / course.published
                                course.unpublished
  
  - analytics.enrollment.queue
    └── Binding: enrollment.events / student.enrolled
```

### **Event Routing Example**

```java
// In PaymentService
rabbitTemplate.convertAndSend(
    "payment.events",           // exchange
    "transaction.completed",    // routing key
    transactionCompletedEvent   // event
);

// In AnalyticsService
@RabbitListener(queues = "analytics.payment.queue")
public void handleTransactionCompleted(TransactionCompletedEvent event) {
    // Process event
}
```

---

## **9. EVENT PROCESSING FLOW - DETAILED**

```
┌─────────────────────────────────────────────────────────────────────┐
│               EVENT PROCESSING FLOW (Detailed)                      │
└─────────────────────────────────────────────────────────────────────┘

External Service                  Message Broker              AnalyticsService
     │                                   │                           │
     │ Business Action                   │                           │
     │ (e.g., Payment)                   │                           │
     ├──────────────┐                    │                           │
     │ Create Event │                    │                           │
     └──────────────┘                    │                           │
     │                                   │                           │
     │ Publish Event                     │                           │
     ├──────────────────────────────────>│                           │
     │                                   │                           │
     │                                   │ Route to Queue            │
     │                                   ├──────────┐                │
     │                                   │ Based on │                │
     │                                   │ Routing  │                │
     │                                   │ Key      │                │
     │                                   └──────────┘                │
     │                                   │                           │
     │                                   │ Deliver Event             │
     │                                   ├──────────────────────────>│
     │                                   │                           │
     │                                   │                           ├───────────┐
     │                                   │                           │ 1. Validate│
     │                                   │                           │ 2. Process │
     │                                   │                           │ 3. Persist │
     │                                   │                           │ 4. ACK     │
     │                                   │                           └───────────┘
     │                                   │<──────────────────────────┤
     │                                   │ Acknowledge               │
     │                                   │                           │
     │                            [Event Processed]                  │
     │                                   │                           │
     │                                   │      Optional: Publish    │
     │                                   │      Internal Event       │
     │                                   │<──────────────────────────┤
     │                                   │ (e.g., Revenue Calculated)│
```

---

## **10. ERROR HANDLING & RETRY STRATEGY**

```
┌─────────────────────────────────────────────────────────────────────┐
│                      ERROR HANDLING STRATEGY                        │
└─────────────────────────────────────────────────────────────────────┘

Event Processing Failed
     │
     ├─ Validation Error?
     │  └─> NACK → Dead Letter Queue → Manual Review
     │
     ├─ Transient Error (DB timeout)?
     │  └─> Retry with exponential backoff (3 attempts)
     │      └─> Success? → ACK
     │      └─> Still fail? → Dead Letter Queue
     │
     └─ Business Logic Error?
        └─> Log Error → ACK (prevent retry loop)
            └─> Send alert to monitoring

Dead Letter Queue Handler:
  - Store failed events
  - Alert admin
  - Manual replay capability
  - Event versioning tracking
```

---

## **11. DATA CONSISTENCY PATTERNS**

### **Idempotency**
```java
@Transactional
public void handleTransactionCompleted(TransactionCompletedEvent event) {
    // Check if already processed (idempotency)
    if (processedEventRepository.existsByEventId(event.getEventId())) {
        log.info("Event already processed: {}", event.getEventId());
        return;  // Skip
    }
    
    // Process event
    processTransaction(event);
    
    // Mark as processed
    processedEventRepository.save(
        new ProcessedEvent(event.getEventId(), LocalDateTime.now())
    );
}
```

### **Eventual Consistency**
- Analytics data có thể lag vài giây/phút so với source data
- Acceptable cho analytics use case
- Display "Last updated: X minutes ago" trên dashboard

---

## **12. PERFORMANCE CONSIDERATIONS**

### **Batch Processing for Heavy Loads**
```java
// Instead of processing one by one
@RabbitListener(queues = "analytics.payment.queue")
public void handleTransactions(List<TransactionCompletedEvent> events) {
    // Batch process
    Map<LocalDate, List<TransactionCompletedEvent>> grouped = 
        events.stream()
              .collect(Collectors.groupingBy(e -> e.getTimestamp().toLocalDate()));
    
    grouped.forEach((date, transactions) -> {
        processDailyRevenueBatch(date, transactions);
    });
}
```

### **Caching Strategy**
```java
// Cache today's DailyRevenue (hot data)
@Cacheable(value = "dailyRevenue", key = "#date")
public DailyRevenue getDailyRevenue(LocalDate date) {
    return dailyRevenueRepository.findByDate(date)
        .orElseGet(() -> createNew(date));
}
```

---

## **SUMMARY - Event-Driven Analytics Service**

### **✅ Pros:**
- **Decoupled**: Analytics không depend vào other services
- **Scalable**: Có thể scale riêng
- **Resilient**: Message broker đảm bảo delivery
- **Flexible**: Dễ thêm analytics mới
- **Real-time**: Analytics update near real-time

### **⚠️ Considerations:**
- **Eventual Consistency**: Data có thể lag
- **Event Versioning**: Cần handle event schema changes
- **Replay Capability**: Cần có khả năng replay events
- **Monitoring**: Cần monitor queue depths, processing times

### **📊 Events Summary:**

| Source Service | Events Published | Analytics Entities Updated |
|----------------|------------------|---------------------------|
| PaymentService | `TransactionCompletedEvent` | DailyRevenue, TransactionSnapshot, InstructorRevenue |
| UserService | `UserRegisteredEvent`<br>`UserLoggedInEvent`<br>`DailyUserStatsEvent` | UserGrowthAnalytics |
| CourseService | `CoursePublishedEvent`<br>`CourseUnpublishedEvent` | InstructorStats |
| EnrollmentService | `StudentEnrolledEvent` | InstructorStats, CourseRevenueSnapshot |
| Scheduled Jobs | Internal triggers | InstructorRevenue, PlatformOverview |

---

**👉 NEXT STEPS (if approved):**
1. Create event listener classes
2. Configure RabbitMQ/Kafka
3. Implement event handlers
4. Add idempotency checks
5. Setup monitoring & alerts

**HÃY CHO TÔI BIẾT NẾU BẠN ĐỒNG Ý VỚI THIẾT KẾ NÀY, HOẶC CẦN ĐIỀU CHỈNH GÌ!**

