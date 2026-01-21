package com.se347.analysticservice.entities.admin.platform;

import com.se347.analysticservice.entities.AbstractAggregateRoot;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.enums.Period;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "platform_overview", indexes = {
        @Index(name = "idx_period_dates", columnList = "period, start_date, end_date"),
        @Index(name = "idx_end_date", columnList = "end_date DESC")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_period_dates", 
            columnNames = {"period", "start_date", "end_date"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformOverview extends AbstractAggregateRoot<PlatformOverview> {
    
    @Id
    private UUID platformOverviewId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Period period;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    // ==================== Current Metrics ====================
    
    // Total users in platform (excluding admins)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_users", nullable = false))
    private Count totalUsers;
    
    // Total enrollments
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_enrollments", nullable = false))
    private Count totalEnrollments;
    
    // Total revenue
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_revenue", nullable = false))
    private Money totalRevenue;

    // Average completion rate
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "avg_retention_rate", nullable = false))
    private Percentage averageCompletionRate;
    
    // ==================== Growth Metrics (New in this period) ====================
    
    // New users in this period
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "new_users_count", nullable = false))
    private Count newUsersCount;
    
    // New enrollments in this period
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "new_enrollments_count", nullable = false))
    private Count newEnrollmentsCount;
    
    // Revenue by period
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "revenue_by_period", nullable = false))
    private Money revenueByPeriod;

    // ==================== Audit Fields ====================
    
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
    
    // ==================== Factory Methods ====================
    
    /**
     * Creates a new PlatformOverview with all required metrics.
     */
    public static PlatformOverview create(
        Period period,
        LocalDate startDate,
        LocalDate endDate,
        Count totalUsers,
        Count totalEnrollments,
        Money totalRevenue,
        Money revenueByPeriod,
        Percentage averageCompletionRate,
        Count newUsersCount,
        Count newEnrollmentsCount
    ) {
        // Validation
        if (period == null) throw new IllegalArgumentException("Period cannot be null");
        if (startDate == null) throw new IllegalArgumentException("Start date cannot be null");
        if (endDate == null) throw new IllegalArgumentException("End date cannot be null");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        // Build entity
        PlatformOverview overview = new PlatformOverview();
        overview.platformOverviewId = UUID.randomUUID(); // Generate ID immediately
        overview.period = period;
        overview.startDate = startDate;
        overview.endDate = endDate;
        overview.totalUsers = totalUsers;
        overview.totalEnrollments = totalEnrollments;
        overview.totalRevenue = totalRevenue;
        overview.revenueByPeriod = revenueByPeriod;
        overview.averageCompletionRate = averageCompletionRate;
        overview.newUsersCount = newUsersCount;
        overview.newEnrollmentsCount = newEnrollmentsCount;
        overview.onCreate();
        
        // Domain event removed - not actively used
        // overview.registerEvent(PlatformOverviewCreatedEvent.now(...));
        
        return overview;
    }
    
    // ==================== Business Methods ====================
    
    /**
     * Calculates average revenue per user.
     */
    public Money averageRevenuePerUser() {
        if (totalUsers.isZero()) {
            return Money.zero();
        }
        return totalRevenue.divide(totalUsers.getValue());
    }
    
    /**
     * Calculates average revenue per enrollment.
     */
    public Money averageRevenuePerEnrollment() {
        if (totalEnrollments.isZero()) {
            return Money.zero();
        }
        return totalRevenue.divide(totalEnrollments.getValue());
    }
    
    /**
     * Calculates enrollment rate (enrollments per user).
     */
    public double enrollmentRate() {
        if (totalUsers.isZero()) {
            return 0.0;
        }
        return totalEnrollments.toDouble() / totalUsers.toDouble();
    }
    
    /**
     * Checks if platform is growing (positive user growth).
     */
    public boolean isGrowing() {
        return newUsersCount != null && !newUsersCount.isZero();
    }
    
    /**
     * Checks if revenue is growing.
     */
    public boolean isRevenueGrowing() {
        return revenueByPeriod != null && revenueByPeriod.isGreaterThanOrEqual(Money.zero());
    }
    
    /**
     * Checks if completion rate meets threshold.
     */
    public boolean hasHealthyCompletionRate(Percentage threshold) {
        return averageCompletionRate != null && averageCompletionRate.isGreaterThan(threshold);
    }
    
    /**
     * Checks if metrics indicate platform health.
     * Healthy = positive user growth + positive revenue growth + completion rate > 50%
     */
    public boolean isPlatformHealthy() {
        return isGrowing() 
            && isRevenueGrowing() 
            && hasHealthyCompletionRate(Percentage.of(50.0));
    }
    
    /**
     * Updates all metrics (for recalculation scenarios).
     */
    public void updateMetrics(
        Count totalUsers,
        Count totalEnrollments,
        Money totalRevenue,
        Money revenueByPeriod,
        Percentage averageCompletionRate,
        Count newUsersCount,
        Count newEnrollmentsCount
    ) {
        this.totalUsers = totalUsers;
        this.totalEnrollments = totalEnrollments;
        this.totalRevenue = totalRevenue;
        this.revenueByPeriod = revenueByPeriod;
        this.averageCompletionRate = averageCompletionRate;
        this.newUsersCount = newUsersCount;
        this.newEnrollmentsCount = newEnrollmentsCount;
        this.onUpdate();
        
        // Domain event removed - not actively used
        // this.registerEvent(PlatformMetricsUpdatedEvent.now(...));
    }
}
