package com.se347.analysticservice.entities.admin.platform;

import com.se347.analysticservice.domains.events.platform.UserGrowthMetricsUpdatedEvent;
import com.se347.analysticservice.domains.events.platform.UserGrowthRecordedEvent;
import com.se347.analysticservice.entities.AbstractAggregateRoot;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_growth_analytics", indexes = {
    @Index(name = "idx_date_desc", columnList = "date DESC"),
    @Index(name = "idx_date_asc", columnList = "date ASC")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_date", columnNames = "date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGrowthAnalytics extends AbstractAggregateRoot<UserGrowthAnalytics> {
    
    @Id
    private UUID userGrowthAnalyticsId;
    
    @Column(nullable = false, unique = true)
    private LocalDate date;
    
    // Number of new users registered on this date (excluding admins)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "new_users_count", nullable = false))
    private Count newUsersCount;
    
    // Number of users who were active on this date (logged in, made action)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "active_users_count", nullable = false))
    private Count activeUsersCount;
    
    // Total accumulated users up to this date (excluding admins)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_users", nullable = false))
    private Count totalUsers;
    
    // Retention rate: percentage of previous day's users who are still active
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "retention_rate"))
    private Percentage retentionRate;
    
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
    
    // ==================== Business Methods ====================
    
    /**
     * Calculates user growth rate compared to previous day.
     * Example: If yesterday had 1000 users and today has 1100, growth rate = 10%
     */
    public Percentage calculateGrowthRate(Count previousDayTotalUsers) {
        return Percentage.growthRate(previousDayTotalUsers.getValue(), totalUsers.getValue());
    }
    
    /**
     * Calculates what percentage of total users are active today.
     * Example: If 1000 total users and 200 active today = 20% engagement
     */
    public Percentage calculateEngagementRate() {
        if (totalUsers.isZero()) {
            return Percentage.zero();
        }
        return activeUsersCount.percentageOf(totalUsers);
    }
    
    /**
     * Checks if growth target is met.
     * Example: Check if we got at least 100 new users today
     */
    public boolean isGrowthTargetMet(Count targetNewUsers) {
        return newUsersCount.isGreaterThanOrEqual(targetNewUsers);
    }
    
    /**
     * Checks if retention rate meets threshold.
     * Example: Check if at least 70% of users are retained
     */
    public boolean isRetentionHealthy(Percentage threshold) {
        return retentionRate.isGreaterThan(threshold);
    }
    
    /**
     * Checks if platform is growing (more new users than yesterday).
     */
    public boolean isGrowing(Count yesterdayNewUsers) {
        return newUsersCount.isGreaterThan(yesterdayNewUsers);
    }

    public static UserGrowthAnalytics create(LocalDate date, Count newUsersCount, Count activeUsersCount, Count totalUsers, Percentage retentionRate) {
        
        if (date == null) throw new IllegalArgumentException("Date cannot be null");
        if (newUsersCount == null) throw new IllegalArgumentException("New users count cannot be null");
        if (activeUsersCount == null) throw new IllegalArgumentException("Active users count cannot be null");
        if (totalUsers == null) throw new IllegalArgumentException("Total users cannot be null");
        if (retentionRate == null) throw new IllegalArgumentException("Retention rate cannot be null");
        
        UserGrowthAnalytics userGrowthAnalytics = new UserGrowthAnalytics();
        userGrowthAnalytics.userGrowthAnalyticsId = UUID.randomUUID(); // Generate ID immediately
        userGrowthAnalytics.date = date;
        userGrowthAnalytics.newUsersCount = newUsersCount;
        userGrowthAnalytics.activeUsersCount = activeUsersCount;
        userGrowthAnalytics.totalUsers = totalUsers;
        userGrowthAnalytics.retentionRate = retentionRate;
        userGrowthAnalytics.onCreate();
        
        // Register domain event (now ID is available)
        userGrowthAnalytics.registerEvent(
            UserGrowthRecordedEvent.now(
                userGrowthAnalytics.userGrowthAnalyticsId,
                date,
                newUsersCount.getValue(),
                activeUsersCount.getValue(),
                totalUsers.getValue(),
                retentionRate.getValue()
            )
        );
        
        return userGrowthAnalytics;
    }
    
    // ==================== Domain Methods for Event Handling ====================
    
    /**
     * Records a new user registration.
     * Business rule: Increment new users count and total users count.
     */
    public void recordNewUser() {
        this.newUsersCount = this.newUsersCount.increment();
        this.totalUsers = this.totalUsers.increment();
        this.onUpdate();
        
        // Register domain event
        this.registerEvent(
            UserGrowthMetricsUpdatedEvent.now(
                this.userGrowthAnalyticsId,
                this.newUsersCount.getValue(),
                this.activeUsersCount.getValue(),
                this.totalUsers.getValue()
            )
        );
    }
    
    /**
     * Records an active user (login, activity).
     * Business rule: Increment active users count if not already counted today.
     */
    public void recordActiveUser() {
        this.activeUsersCount = this.activeUsersCount.increment();
        this.onUpdate();
        
        // Register domain event
        this.registerEvent(
            UserGrowthMetricsUpdatedEvent.now(
                this.userGrowthAnalyticsId,
                this.newUsersCount.getValue(),
                this.activeUsersCount.getValue(),
                this.totalUsers.getValue()
            )
        );
    }
    
    /**
     * Updates only the total users count.
     * Used when syncing with actual user count from database.
     */
    public void updateTotalUsers(Count totalUsers) {
        if (totalUsers == null) {
            throw new IllegalArgumentException("Total users cannot be null");
        }
        
        this.totalUsers = totalUsers;
        this.onUpdate();
    }
    
    /**
     * Calculates and updates retention rate compared to previous day.
     * Business rule: Retention = (today's active users / yesterday's active users) * 100%
     */
    public Percentage calculateRetentionRate(Count previousDayActiveUsers) {
        if (previousDayActiveUsers.isZero()) {
            return Percentage.zero();
        }
        
        return this.activeUsersCount.percentageOf(previousDayActiveUsers);
    }
    
    /**
     * Updates the retention rate.
     */
    public void updateRetentionRate(Percentage retentionRate) {
        if (retentionRate == null) {
            throw new IllegalArgumentException("Retention rate cannot be null");
        }
        
        this.retentionRate = retentionRate;
        this.onUpdate();
    }
}
