package com.se347.analysticservice.entities.admin.revenue;

import com.se347.analysticservice.domains.events.revenue.DailyRevenueCreatedEvent;
import com.se347.analysticservice.domains.events.revenue.DailyRevenueUpdatedEvent;
import com.se347.analysticservice.entities.AbstractAggregateRoot;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DailyRevenue Aggregate Root - Tracks daily revenue metrics.
 * Simplified to track only essential revenue data without category breakdown.
 */
@Entity
@Table(name = "daily_revenue", indexes = {
    @Index(name = "idx_date_desc", columnList = "date DESC"),
    @Index(name = "idx_date_revenue", columnList = "date, total_revenue DESC")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_date", columnNames = "date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRevenue extends AbstractAggregateRoot<DailyRevenue> {
    
    @Id
    private UUID dailyRevenueId;
    
    @Column(nullable = false, unique = true)
    private LocalDate date;
    
    // Total revenue for the day
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_revenue", nullable = false))
    private Money totalRevenue;
    
    // Total number of transactions
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_transactions", nullable = false))
    private Count totalTransactions;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static DailyRevenue create(LocalDate date, Money totalRevenue, Count totalTransactions) {
        if (date == null) throw new IllegalArgumentException("Date cannot be null");
        if (totalRevenue == null) throw new IllegalArgumentException("Total revenue cannot be null");
        if (totalTransactions == null) throw new IllegalArgumentException("Total transactions cannot be null");

        DailyRevenue dailyRevenue = new DailyRevenue();
        dailyRevenue.dailyRevenueId = UUID.randomUUID(); // Generate ID immediately
        dailyRevenue.date = date;
        dailyRevenue.totalRevenue = totalRevenue;
        dailyRevenue.totalTransactions = totalTransactions;
        dailyRevenue.onCreate();
        
        // Register domain event (now ID is available)
        dailyRevenue.registerEvent(
            DailyRevenueCreatedEvent.now(
                dailyRevenue.dailyRevenueId,
                date,
                totalRevenue.getAmount(),
                totalTransactions.getValue()
            )
        );
        
        return dailyRevenue;
    }

    // ==================== Business Methods ====================
    
    /**
     * Calculates average transaction value.
     */
    public Money averageTransactionValue() {
        if (totalTransactions.isZero()) {
            return Money.zero();
        }
        return totalRevenue.divide(totalTransactions.getValue());
    }
    
    /**
     * Checks if revenue goal is met (example: $10,000/day).
     */
    public boolean isRevenueGoalMet(Money goal) {
        return totalRevenue.isGreaterThanOrEqual(goal);
    }

    public DailyRevenue updateMetrics(Money totalRevenue, Count totalTransactions) {
        if (totalRevenue == null) throw new IllegalArgumentException("Total revenue cannot be null");
        if (totalTransactions == null) throw new IllegalArgumentException("Total transactions cannot be null");

        this.totalRevenue = totalRevenue;
        this.totalTransactions = totalTransactions;
        this.onUpdate();
        
        // Register domain event
        this.registerEvent(
            DailyRevenueUpdatedEvent.now(
                this.dailyRevenueId,
                totalRevenue.getAmount(),
                totalTransactions.getValue()
            )
        );
        
        return this;
    }
}
