package com.se347.analysticservice.entities.admin.revenue;

import com.se347.analysticservice.domains.events.revenue.InstructorRevenueCalculatedEvent;
import com.se347.analysticservice.entities.AbstractAggregateRoot;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.enums.Period;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * InstructorRevenue Aggregate Root - Tracks instructor revenue for a given period.
 * Simplified: Only tracks total revenue received, no commission/tax calculations.
 */
@Entity
@Table(name = "instructor_revenue", indexes = {
    @Index(name = "idx_instructor_period", columnList = "instructor_id, period, end_date DESC"),
    @Index(name = "idx_revenue_ranking", columnList = "period, end_date, total_revenue DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstructorRevenue extends AbstractAggregateRoot<InstructorRevenue> {
    
    @Id
    private UUID instructorRevenueId;
    
    @Column(nullable = false)
    private UUID instructorId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Period period;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    // Total revenue earned in this period
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_revenue", nullable = false))
    private Money totalRevenue;
    
    // Performance metrics
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_enrollments"))
    private Count totalEnrollments;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_courses"))
    private Count totalCourses;
    
    // Top performing courses
    @OneToMany(mappedBy = "instructorRevenue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseRevenueSnapshot> topPerformingCourses = new ArrayList<>();
    
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
     * Creates a new InstructorRevenue record for a period.
     */
    public static InstructorRevenue create(
        UUID instructorId,
        Period period,
        LocalDate startDate,
        LocalDate endDate,
        Money totalRevenue,
        Count totalEnrollments,
        Count totalCourses
    ) {
        if (instructorId == null) throw new IllegalArgumentException("Instructor ID cannot be null");
        if (period == null) throw new IllegalArgumentException("Period cannot be null");
        if (startDate == null) throw new IllegalArgumentException("Start date cannot be null");
        if (endDate == null) throw new IllegalArgumentException("End date cannot be null");
        if (totalRevenue == null) throw new IllegalArgumentException("Total revenue cannot be null");
        if (totalEnrollments == null) throw new IllegalArgumentException("Total enrollments cannot be null");
        if (totalCourses == null) throw new IllegalArgumentException("Total courses cannot be null");
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        
        InstructorRevenue revenue = new InstructorRevenue();
        revenue.instructorRevenueId = UUID.randomUUID(); // Generate ID immediately
        revenue.instructorId = instructorId;
        revenue.period = period;
        revenue.startDate = startDate;
        revenue.endDate = endDate;
        revenue.totalRevenue = totalRevenue;
        revenue.totalEnrollments = totalEnrollments;
        revenue.totalCourses = totalCourses;
        revenue.topPerformingCourses = new ArrayList<>();
        revenue.onCreate();
        
        // Register domain event (now ID is available)
        revenue.registerEvent(
            InstructorRevenueCalculatedEvent.now(
                revenue.instructorRevenueId,
                instructorId,
                period,
                startDate,
                endDate,
                totalRevenue.getAmount(),
                totalEnrollments.getValue(),
                totalCourses.getValue()
            )
        );
        
        return revenue;
    }
    
    // ==================== Business Methods ====================
    
    /**
     * Calculates average revenue per course.
     */
    public Money averageRevenuePerCourse() {
        if (totalCourses.isZero()) {
            return Money.zero();
        }
        return totalRevenue.divide(totalCourses.getValue());
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
     * Checks if instructor revenue meets threshold.
     */
    public boolean meetsRevenueThreshold(Money threshold) {
        return totalRevenue.isGreaterThanOrEqual(threshold);
    }
    
    /**
     * Adds a top performing course to this revenue record.
     */
    public void addTopCourse(CourseRevenueSnapshot courseSnapshot) {
        if (courseSnapshot == null) {
            throw new IllegalArgumentException("Course snapshot cannot be null");
        }
        this.topPerformingCourses.add(courseSnapshot);
    }
    
    /**
     * Updates revenue metrics.
     */
    public void updateMetrics(Money totalRevenue, Count totalEnrollments, Count totalCourses) {
        if (totalRevenue == null) throw new IllegalArgumentException("Total revenue cannot be null");
        if (totalEnrollments == null) throw new IllegalArgumentException("Total enrollments cannot be null");
        if (totalCourses == null) throw new IllegalArgumentException("Total courses cannot be null");
        
        this.totalRevenue = totalRevenue;
        this.totalEnrollments = totalEnrollments;
        this.totalCourses = totalCourses;
        this.onUpdate();
    }
}
