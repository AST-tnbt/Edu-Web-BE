# Domain Events Update Summary

## **Tổng quan**
Đã cập nhật toàn bộ domain events cho admin bounded context để phù hợp với cấu trúc entities đã được simplified (bỏ commission/fee tracking).

---

## **1. Revenue Events**

### **DailyRevenueCreatedEvent** ✅ Updated
**Trigger:** Khi tạo daily revenue record mới

**Before:**
```java
UUID dailyRevenueId
LocalDate date
BigDecimal initialRevenue  // ❌ Vague naming
```

**After:**
```java
UUID dailyRevenueId
LocalDate date
BigDecimal totalRevenue     // ✅ Clear naming
Long totalTransactions      // ✅ Added transaction count
```

**Published in:** `DailyRevenue.create()`

---

### **DailyRevenueUpdatedEvent** ✅ New
**Trigger:** Khi update revenue metrics

```java
UUID dailyRevenueId
BigDecimal newTotalRevenue
Long newTotalTransactions
```

**Published in:** `DailyRevenue.updateMetrics()`

**Use case:** Khi có transactions mới trong ngày, update metrics

---

### **InstructorRevenueCalculatedEvent** ✅ Updated
**Trigger:** Khi tính instructor revenue cho period

**Before:**
```java
BigDecimal grossRevenue      // ❌ Removed (no commission tracking)
BigDecimal netRevenue        // ❌ Removed
BigDecimal platformFee       // ❌ Removed
```

**After:**
```java
UUID instructorRevenueId
UUID instructorId
Period period
LocalDate startDate
LocalDate endDate
BigDecimal totalRevenue      // ✅ Simplified
Long totalEnrollments        // ✅ Added
Long totalCourses            // ✅ Added
```

**Published in:** `InstructorRevenue.create()`

**Use case:** 
- Trigger payout scheduling
- Update instructor dashboard
- Revenue report generation

---

### **Deleted Events** ❌

#### **CommissionDueEvent** - Deleted
**Lý do:** Không còn track commission/fee

#### **TransactionAddedEvent** - Deleted
**Lý do:** Quá granular, không cần event cho mỗi transaction

#### **DailyRevenueFinalizedEvent** - Deleted
**Lý do:** Simplified flow, không cần finalize step

---

## **2. Platform Events**

### **PlatformOverviewCreatedEvent** ✅ Existing (No change)
**Trigger:** Khi tạo platform overview cho period

```java
UUID platformOverviewId
Period period
LocalDate startDate
LocalDate endDate
```

**Published in:** `PlatformOverview.create()`

---

### **PlatformMetricsUpdatedEvent** ✅ Existing (No change)
**Trigger:** Khi update platform metrics

```java
UUID platformOverviewId
Long totalUsers
Long totalCourses
Long totalEnrollments
```

**Published in:** `PlatformOverview.updateMetrics()`

---

### **UserGrowthRecordedEvent** ✅ Updated
**Trigger:** Khi record user growth cho date mới

```java
UUID userGrowthAnalyticsId
LocalDate date
Long newUsersCount
Long activeUsersCount
Long totalUsers
Double retentionRate
```

**Published in:** `UserGrowthAnalytics.create()`

**Use case:**
- Daily growth tracking
- Dashboard real-time updates
- Alert system (nếu growth thấp)

---

### **UserGrowthMetricsUpdatedEvent** ✅ New
**Trigger:** Khi recalculate user growth metrics

```java
UUID userGrowthAnalyticsId
Long newUsersCount
Long activeUsersCount
Long totalUsers
```

**Published in:** `UserGrowthAnalytics.updateMetrics()`

---

### **Deleted Events** ❌

#### **UserMetricsUpdatedEvent** - Deleted (Duplicate)
**Lý do:** Duplicate với `UserGrowthMetricsUpdatedEvent`

---

## **3. Instructor Events**

### **InstructorStatsCreatedEvent** ✅ Updated
**Trigger:** Khi tạo instructor stats record

**Before:**
```java
// Complex structure with many fields
```

**After:**
```java
UUID instructorStatsId
UUID instructorId
Long totalCourses
Long totalStudents
```

**Published in:** `InstructorStats.create()`

**Use case:**
- Initialize instructor dashboard
- Welcome email to instructor
- Analytics initialization

---

### **InstructorStatsUpdatedEvent** ✅ New
**Trigger:** Khi update instructor stats

```java
UUID instructorStatsId
UUID instructorId
Long totalCourses
Long totalStudents
```

**Published in:** `InstructorStats.updateMetrics()`

**Use case:**
- Real-time dashboard updates
- Achievement/milestone triggers
- Ranking recalculation

---

### **Deleted Events** ❌

#### **CourseAddedToInstructorEvent** - Deleted
#### **StudentsAddedToInstructorEvent** - Deleted
#### **InstructorStatusChangedEvent** - Deleted
#### **InstructorSuspendedEvent** - Deleted
#### **InstructorActivatedEvent** - Deleted
#### **InstructorInactivityDetectedEvent** - Deleted

**Lý do:** Quá chi tiết cho simplified design. Instructor analytics chỉ cần track tổng số courses và students.

---

## **4. Event Architecture**

### **Domain Event Interface**
```java
public interface DomainEvent {
    UUID getEventId();
    LocalDateTime getOccurredAt();
}
```

### **Event Pattern: @Value + Static Factory**
```java
@Value
public class DailyRevenueCreatedEvent implements DomainEvent {
    UUID eventId;
    UUID dailyRevenueId;
    LocalDate date;
    BigDecimal totalRevenue;
    Long totalTransactions;
    LocalDateTime occurredAt;
    
    public static DailyRevenueCreatedEvent now(
        UUID dailyRevenueId,
        LocalDate date,
        BigDecimal totalRevenue,
        Long totalTransactions
    ) {
        return new DailyRevenueCreatedEvent(
            UUID.randomUUID(),        // Auto-generate eventId
            dailyRevenueId,
            date,
            totalRevenue,
            totalTransactions,
            LocalDateTime.now()       // Auto-set timestamp
        );
    }
}
```

**Benefits:**
- ✅ Immutable (Lombok `@Value`)
- ✅ Easy to create (`now()` factory method)
- ✅ Auto-generate eventId và timestamp
- ✅ Type-safe

---

## **5. Event Publishing trong Entities**

### **Pattern:**
```java
@Entity
public class DailyRevenue extends AbstractAggregateRoot<DailyRevenue> {
    
    public static DailyRevenue create(...) {
        DailyRevenue revenue = new DailyRevenue();
        // ... set fields ...
        
        // Register event
        revenue.registerEvent(
            DailyRevenueCreatedEvent.now(...)
        );
        
        return revenue;
    }
    
    public void updateMetrics(...) {
        // ... update fields ...
        
        // Register event
        this.registerEvent(
            DailyRevenueUpdatedEvent.now(...)
        );
    }
}
```

**AbstractAggregateRoot** handle event registration và publishing.

---

## **6. Event Usage trong System**

### **Event Flow:**
```
1. Entity method called
   ↓
2. Business logic executed
   ↓
3. Domain event registered (via registerEvent())
   ↓
4. Entity persisted (via Repository)
   ↓
5. Events published (via @EventListener trong Spring)
   ↓
6. Event handlers execute
   ↓
7. External systems notified (via Message Broker)
```

### **Example Use Cases:**

#### **DailyRevenueCreatedEvent:**
- ✅ Trigger email to admin: "Daily revenue report ready"
- ✅ Update cache for dashboard
- ✅ Send to analytics service for ML

#### **InstructorRevenueCalculatedEvent:**
- ✅ Trigger payout scheduling
- ✅ Send notification to instructor: "Your earnings for this month: $X"
- ✅ Update instructor ranking

#### **UserGrowthRecordedEvent:**
- ✅ Alert if growth < threshold
- ✅ Update marketing dashboard
- ✅ Trigger A/B test analysis

---

## **7. Event List Summary**

### **Revenue Context (3 events)**
| Event | Status | Trigger |
|-------|--------|---------|
| DailyRevenueCreatedEvent | ✅ Updated | Create daily revenue |
| DailyRevenueUpdatedEvent | ✅ New | Update daily revenue |
| InstructorRevenueCalculatedEvent | ✅ Updated | Calculate instructor revenue |

### **Platform Context (4 events)**
| Event | Status | Trigger |
|-------|--------|---------|
| PlatformOverviewCreatedEvent | ✅ Existing | Create platform overview |
| PlatformMetricsUpdatedEvent | ✅ Existing | Update platform metrics |
| UserGrowthRecordedEvent | ✅ Updated | Record user growth |
| UserGrowthMetricsUpdatedEvent | ✅ New | Update user growth |

### **Instructor Context (2 events)**
| Event | Status | Trigger |
|-------|--------|---------|
| InstructorStatsCreatedEvent | ✅ Updated | Create instructor stats |
| InstructorStatsUpdatedEvent | ✅ New | Update instructor stats |

**Total: 9 events** (simplified từ 16+ events cũ)

---

## **8. Migration Notes**

### **Breaking Changes:**
1. **DailyRevenueCreatedEvent** - Added `totalTransactions` field
2. **InstructorRevenueCalculatedEvent** - Removed `grossRevenue`, `netRevenue`, `platformFee`
3. **Deleted events** - Event listeners cần được remove

### **Migration Steps:**
1. Update event handlers cho updated events
2. Remove handlers cho deleted events
3. Add handlers cho new events
4. Update message broker routing keys nếu cần
5. Update event documentation

---

## **9. Best Practices Applied**

### **✅ Event Naming:**
- Past tense (Created, Updated, Calculated)
- Clear business meaning
- Domain language

### **✅ Event Content:**
- Include aggregate ID
- Include enough data để handlers không cần query
- Avoid lazy loading triggers
- Primitives instead of Value Objects (cho serialization)

### **✅ Event Size:**
- Keep events small
- Only essential data
- Avoid large objects

### **✅ Event Versioning:**
- Use static factory methods
- Easy to add new fields (với defaults)
- Backward compatible

---

## **Kết luận**

Domain events đã được:
- ✅ **Simplified**: Bỏ commission/fee tracking
- ✅ **Aligned**: Phù hợp với entities structure
- ✅ **Complete**: Tất cả CRUD operations có events
- ✅ **Consistent**: Follow cùng pattern
- ✅ **Documented**: Clear use cases

**Result:** Clean event-driven architecture cho admin analytics context! 🎉

