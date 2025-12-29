package com.se347.analysticservice.entities.admin.revenue;

import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * CourseRevenueSnapshot - Snapshot of a course's revenue performance.
 * Simplified: Only tracks total revenue, no share calculations.
 */
@Entity
@Table(name = "course_revenue_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRevenueSnapshot {
    
    @Id
    private UUID courseRevenueSnapshotId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_revenue_id", nullable = false)
    private InstructorRevenue instructorRevenue;
    
    @Column(nullable = false)
    private UUID courseId;
    
    @Column(nullable = false, length = 500)
    private String courseTitle;
    
    // Number of enrollments for this course
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "enrollment_count"))
    private Count enrollmentCount;
    
    // Total revenue from this course
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "revenue", nullable = false))
    private Money revenue;
    
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
     * Creates a new course revenue snapshot.
     */
    public static CourseRevenueSnapshot create(
        InstructorRevenue instructorRevenue,
        UUID courseId,
        String courseTitle,
        Count enrollmentCount,
        Money revenue
    ) {
        if (instructorRevenue == null) throw new IllegalArgumentException("Instructor revenue cannot be null");
        if (courseId == null) throw new IllegalArgumentException("Course ID cannot be null");
        if (courseTitle == null || courseTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be null or empty");
        }
        if (enrollmentCount == null) throw new IllegalArgumentException("Enrollment count cannot be null");
        if (revenue == null) throw new IllegalArgumentException("Revenue cannot be null");

        CourseRevenueSnapshot snapshot = new CourseRevenueSnapshot();
        snapshot.courseRevenueSnapshotId = UUID.randomUUID(); // Generate ID immediately
        snapshot.instructorRevenue = instructorRevenue;
        snapshot.courseId = courseId;
        snapshot.courseTitle = courseTitle;
        snapshot.enrollmentCount = enrollmentCount;
        snapshot.revenue = revenue;
        snapshot.onCreate();
        
        return snapshot;
    }

    // ==================== Business Methods ====================
    
    /**
     * Calculates average revenue per enrollment.
     */
    public Money averageRevenuePerEnrollment() {
        if (enrollmentCount.isZero()) {
            return Money.zero();
        }
        return revenue.divide(enrollmentCount.getValue());
    }
    
    /**
     * Updates course metrics.
     */
    public void updateMetrics(Count enrollmentCount, Money revenue) {
        if (enrollmentCount == null) throw new IllegalArgumentException("Enrollment count cannot be null");
        if (revenue == null) throw new IllegalArgumentException("Revenue cannot be null");

        this.enrollmentCount = enrollmentCount;
        this.revenue = revenue;
        this.onUpdate();
    }
}
