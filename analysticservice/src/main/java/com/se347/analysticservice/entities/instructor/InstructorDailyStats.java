package com.se347.analysticservice.entities.instructor;

import com.se347.analysticservice.entities.AbstractAggregateRoot;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "instructor_daily_stats",
    indexes = {
        @Index(name = "idx_instructor_date", columnList = "instructor_id, date DESC"),
        @Index(name = "idx_date_desc", columnList = "date DESC")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_instructor_date",
            columnNames = {"instructor_id", "date"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstructorDailyStats extends AbstractAggregateRoot<InstructorDailyStats> {

    @Id
    private UUID instructorDailyStatsId;

    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;

    @Column(nullable = false)
    private LocalDate date;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "new_enrollments", nullable = false))
    private Count newEnrollments;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "active_students", nullable = false))
    private Count activeStudents;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "daily_revenue", nullable = false))
    private Money dailyRevenue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "course_completions", nullable = false))
    private Count courseCompletionsCount;

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

    public static InstructorDailyStats create(
        UUID instructorId,
        LocalDate date,
        Count newEnrollments,
        Count activeStudents,
        Money dailyRevenue,
        Count courseCompletions
    ) {
        if (instructorId == null) {
            throw new IllegalArgumentException("Instructor ID cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (newEnrollments == null) {
            throw new IllegalArgumentException("New enrollments cannot be null");
        }
        if (activeStudents == null) {
            throw new IllegalArgumentException("Active students cannot be null");
        }
        if (dailyRevenue == null) {
            throw new IllegalArgumentException("Daily revenue cannot be null");
        }
        if (courseCompletions == null) {
            throw new IllegalArgumentException("Course completions cannot be null");
        }

        InstructorDailyStats stats = new InstructorDailyStats();
        stats.instructorDailyStatsId = UUID.randomUUID();
        stats.instructorId = instructorId;
        stats.date = date;
        stats.newEnrollments = newEnrollments;
        stats.activeStudents = activeStudents;
        stats.dailyRevenue = dailyRevenue;
        stats.courseCompletionsCount = courseCompletions;
        stats.onCreate();

        return stats;
    }

    public void recordEnrollment() {
        this.newEnrollments = this.newEnrollments.increment();
        this.onUpdate();
    }

    public void recordActiveStudent() {
        this.activeStudents = this.activeStudents.increment();
        this.onUpdate();
    }

    public void recordRevenue(Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        this.dailyRevenue = this.dailyRevenue.add(amount);
        this.onUpdate();
    }

    public void recordCourseCompletion() {
        this.courseCompletionsCount = this.courseCompletionsCount.increment();
        this.onUpdate();
    }

    public Count getCourseCompletions() {
        return courseCompletionsCount;
    }
}

