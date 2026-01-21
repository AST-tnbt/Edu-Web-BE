package com.se347.analysticservice.entities.instructor;

import com.se347.analysticservice.entities.AbstractAggregateRoot;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "instructor_course_stats",
    indexes = {
        @Index(name = "idx_instructor_course", columnList = "instructor_id, course_id"),
        @Index(name = "idx_revenue_desc", columnList = "total_revenue DESC"),
        @Index(name = "idx_completion_rate_desc", columnList = "completion_rate DESC")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_instructor_course",
            columnNames = {"instructor_id", "course_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstructorCourseStats extends AbstractAggregateRoot<InstructorCourseStats> {

    @Id
    private UUID instructorCourseStatsId;

    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_students", nullable = false))
    private Count totalStudents;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_revenue", nullable = false))
    private Money totalRevenue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "completion_rate"))
    private Percentage completionRatePercent;

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

    public static InstructorCourseStats create(
        UUID instructorId,
        UUID courseId,
        Count totalStudents,
        Money totalRevenue,
        Percentage completionRate
    ) {
        if (instructorId == null) {
            throw new IllegalArgumentException("Instructor ID cannot be null");
        }
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        if (totalStudents == null) {
            throw new IllegalArgumentException("Total students cannot be null");
        }
        if (totalRevenue == null) {
            throw new IllegalArgumentException("Total revenue cannot be null");
        }

        InstructorCourseStats stats = new InstructorCourseStats();
        stats.instructorCourseStatsId = UUID.randomUUID();
        stats.instructorId = instructorId;
        stats.courseId = courseId;
        stats.totalStudents = totalStudents;
        stats.totalRevenue = totalRevenue;
        stats.completionRatePercent = completionRate != null ? completionRate : Percentage.zero();
        stats.onCreate();

        return stats;
    }

    public void recordEnrollment(Count count) {
        if (count == null) {
            throw new IllegalArgumentException("Count cannot be null");
        }

        this.totalStudents = this.totalStudents.add(count);
        this.onUpdate();
    }

    public void recordRevenue(Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        this.totalRevenue = this.totalRevenue.add(amount);
        this.onUpdate();
    }

    public void updateCompletionRate(Percentage completionRate) {
        if (completionRate == null) {
            throw new IllegalArgumentException("Completion rate cannot be null");
        }

        this.completionRatePercent = this.completionRatePercent.add(completionRate);
        this.onUpdate();
    }

    /**
     * Backward-compatible getter to avoid breaking older code/DTOs.
     * Prefer {@link #getCompletionRatePercent()} for clarity.
     */
    public Percentage getCompletionRate() {
        return completionRatePercent;
    }
}

