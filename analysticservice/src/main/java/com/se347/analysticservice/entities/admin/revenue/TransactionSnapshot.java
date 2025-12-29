package com.se347.analysticservice.entities.admin.revenue;

import com.se347.analysticservice.entities.shared.valueobjects.Money;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TransactionSnapshot - Immutable snapshot of a transaction.
 * Simplified: Only tracks the transaction amount, no fee calculations.
 */
@Entity
@Table(name = "transaction_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionSnapshot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_revenue_id", nullable = false)
    private DailyRevenue dailyRevenue;
    
    @Column(nullable = false)
    private UUID transactionId;
    
    @Column(nullable = false)
    private UUID enrollmentId;
    
    @Column(nullable = false)
    private UUID courseId;
    
    @Column(nullable = false)
    private UUID studentId;
    
    @Column(nullable = false)
    private UUID instructorId;
    
    // Transaction amount
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false))
    private Money amount;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    // ==================== Factory Methods ====================
    
    /**
     * Creates a new transaction snapshot.
     */
    public static TransactionSnapshot create(
        DailyRevenue dailyRevenue,
        UUID transactionId,
        UUID enrollmentId,
        UUID courseId,
        UUID studentId,
        UUID instructorId,
        Money amount,
        LocalDateTime timestamp
    ) {
        if (dailyRevenue == null) throw new IllegalArgumentException("Daily revenue cannot be null");
        if (transactionId == null) throw new IllegalArgumentException("Transaction ID cannot be null");
        if (enrollmentId == null) throw new IllegalArgumentException("Enrollment ID cannot be null");
        if (courseId == null) throw new IllegalArgumentException("Course ID cannot be null");
        if (studentId == null) throw new IllegalArgumentException("Student ID cannot be null");
        if (instructorId == null) throw new IllegalArgumentException("Instructor ID cannot be null");
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        if (timestamp == null) throw new IllegalArgumentException("Timestamp cannot be null");
        
        TransactionSnapshot snapshot = new TransactionSnapshot();
        snapshot.dailyRevenue = dailyRevenue;
        snapshot.transactionId = transactionId;
        snapshot.enrollmentId = enrollmentId;
        snapshot.courseId = courseId;
        snapshot.studentId = studentId;
        snapshot.instructorId = instructorId;
        snapshot.amount = amount;
        snapshot.timestamp = timestamp;
        
        return snapshot;
    }
}
