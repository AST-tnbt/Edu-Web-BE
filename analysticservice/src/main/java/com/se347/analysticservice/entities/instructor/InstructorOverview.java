package com.se347.analysticservice.entities.instructor;

import com.se347.analysticservice.entities.AbstractAggregateRoot;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "instructor_overview",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_instructor_overview_instructor_id", columnNames = "instructor_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstructorOverview extends AbstractAggregateRoot<InstructorOverview> {

    @Id
    private UUID instructorOverviewId;

    @Column(name = "instructor_id", nullable = false, unique = true)
    private UUID instructorId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_courses", nullable = false))
    private Count totalCourses;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_students", nullable = false))
    private Count totalStudents;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_revenue", nullable = false))
    private Money totalRevenue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "average_completion_rate"))
    private Percentage averageCompletionRatePercent;

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

    public static InstructorOverview create(
        UUID instructorId,
        Count totalCourses,
        Count totalStudents,
        Money totalRevenue,
        Percentage averageCompletionRate
    ) {
        if (instructorId == null) {
            throw new IllegalArgumentException("Instructor ID cannot be null");
        }
        if (totalCourses == null) {
            throw new IllegalArgumentException("Total courses cannot be null");
        }
        if (totalStudents == null) {
            throw new IllegalArgumentException("Total students cannot be null");
        }
        if (totalRevenue == null) {
            throw new IllegalArgumentException("Total revenue cannot be null");
        }

        InstructorOverview overview = new InstructorOverview();
        overview.instructorOverviewId = UUID.randomUUID();
        overview.instructorId = instructorId;
        overview.totalCourses = totalCourses;
        overview.totalStudents = totalStudents;
        overview.totalRevenue = totalRevenue;
        overview.averageCompletionRatePercent = averageCompletionRate != null ? averageCompletionRate : Percentage.zero();
        overview.onCreate();

        return overview;
    }

    public void updateMetrics(
        Count totalCourses,
        Count totalStudents,
        Money totalRevenue,
        Percentage averageCompletionRate
    ) {
        if (totalCourses == null) {
            throw new IllegalArgumentException("Total courses cannot be null");
        }
        if (totalStudents == null) {
            throw new IllegalArgumentException("Total students cannot be null");
        }
        if (totalRevenue == null) {
            throw new IllegalArgumentException("Total revenue cannot be null");
        }

        this.totalCourses = totalCourses;
        this.totalStudents = totalStudents;
        this.totalRevenue = totalRevenue;
        this.averageCompletionRatePercent = averageCompletionRate != null ? averageCompletionRate : Percentage.zero();
        this.onUpdate();
    }

    public void updateAverageCompletionRate(Percentage averageCompletionRate) {
        if (averageCompletionRate == null) {
            throw new IllegalArgumentException("Average completion rate cannot be null");
        }

        this.averageCompletionRatePercent = averageCompletionRate;
        this.onUpdate();
    }

    public void recordEnrollmentCompletionRateUpdate(UUID courseId, UUID enrollmentId, 
                                                      Double previousEnrollmentRate, 
                                                      Double newEnrollmentRate) {
        if (courseId == null) throw new IllegalArgumentException("Course ID cannot be null");
        if (enrollmentId == null) throw new IllegalArgumentException("Enrollment ID cannot be null");
        if (newEnrollmentRate == null) throw new IllegalArgumentException("New enrollment completion rate cannot be null");
        
        if (this.totalCourses.isZero()) {
            throw new IllegalStateException("Cannot update completion rate: instructor has no courses");
        }
        
        if (previousEnrollmentRate == null) {
            previousEnrollmentRate = 0.0;
        }
        
        double currentInstructorAverage = this.averageCompletionRatePercent.getValue();
        long totalCourses = this.totalCourses.getValue();
        
        if (totalCourses == 0) {
            return;
        }
        
        double estimatedTotalCourseRates = currentInstructorAverage * totalCourses;
        double estimatedCourseAverage = estimatedTotalCourseRates / totalCourses;
        
        double enrollmentRateChange = newEnrollmentRate - previousEnrollmentRate;
        
        double estimatedEnrollmentsPerCourse = this.totalStudents.getValue() / (double) totalCourses;
        if (estimatedEnrollmentsPerCourse == 0) {
            estimatedEnrollmentsPerCourse = 1.0;
        }
        
        double courseRateChange = enrollmentRateChange / estimatedEnrollmentsPerCourse;
        double newCourseAverage = estimatedCourseAverage + courseRateChange;
        newCourseAverage = Math.max(0.0, Math.min(100.0, newCourseAverage));
        
        double newTotalCourseRates = estimatedTotalCourseRates - estimatedCourseAverage + newCourseAverage;
        double newInstructorAverage = newTotalCourseRates / totalCourses;
        
        this.averageCompletionRatePercent = Percentage.of(Math.max(0.0, Math.min(100.0, newInstructorAverage)));
        this.onUpdate();
    }

    public Percentage getAverageCompletionRate() {
        return averageCompletionRatePercent;
    }
}


