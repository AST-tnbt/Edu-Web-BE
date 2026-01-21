package com.se347.enrollmentservice.entities;

import com.se347.enrollmentservice.domains.events.UpdateOverallProgressEvent;
import com.se347.enrollmentservice.entities.valueobjects.Percentage;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA only
public class CourseProgress {
    
    @Id
    private UUID courseProgressId; //courseProgressId = enrollmentId

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Embedded
    private Percentage overallProgress;

    @Column(nullable = false)
    private Integer lessonsCompleted;

    @Column(nullable = false)
    private Integer totalLessons;

    @Column(nullable = false)
    private boolean isAllLessonsCompleted;

    @Column(nullable = true)
    private LocalDateTime allLessonsCompletedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = true)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== FACTORY METHOD (Package-private) ==========
    
    /**
     * Create initial course progress
     * 
     * PACKAGE-PRIVATE: Can only be called by Enrollment (same package)
     */
    static CourseProgress createFor(Enrollment enrollment, int totalLessons) {
        guardAgainstNull(enrollment, "Enrollment");
        guardAgainstNegative(totalLessons, "Total lessons");
        
        CourseProgress progress = new CourseProgress();
        progress.courseProgressId = enrollment.getEnrollmentId();
        progress.enrollment = enrollment;
        progress.totalLessons = totalLessons;
        progress.lessonsCompleted = 0;
        progress.overallProgress = Percentage.zero();
        progress.isAllLessonsCompleted = false;
        progress.allLessonsCompletedAt = null;
        return progress;
    }

    // ========== DOMAIN METHODS ==========
    
    public CourseProgress updateTotalLessons(int newTotal) {
        guardAgainstNegative(newTotal, "Total lessons");
        
        this.totalLessons = newTotal;
        this.recalculateProgress();
        return this;
    }
    
    /**
     * Increment completed lessons count
     * 
     * BUSINESS RULE: 
     * - Can't exceed total lessons
     * - Auto-complete course if all lessons done
     */
    public void incrementCompletedLessons() {
        if (this.lessonsCompleted >= this.totalLessons) {
            return; // Already at max, no-op (safety check)
        }
        
        this.lessonsCompleted++;
        this.recalculateProgress();
        
        // Check if course is now completed
        if (this.lessonsCompleted >= this.totalLessons && !this.isAllLessonsCompleted) {
            this.markAllLessonsAsCompleted();
        }
    }
    
    /**
     * Get the UpdateOverallProgressEvent for the current progress state.
     * This event should be registered by the Enrollment aggregate root.
     */
    public UpdateOverallProgressEvent createUpdateOverallProgressEvent() {
        return UpdateOverallProgressEvent.now(
            this.enrollment.getEnrollmentId(),
            this.enrollment.getCourseId(),
            this.enrollment.getStudentId(),
            this.enrollment.getInstructorId(),
            this.getProgressPercentage()
        );
    }

    /**
     * Mark entire course as completed
     * 
     * BUSINESS RULE: Can only complete if all lessons are done
     */
    private void markAllLessonsAsCompleted() {
        if (this.lessonsCompleted < this.totalLessons) {
            throw new IllegalStateException(
                "Cannot complete course with " + this.lessonsCompleted + 
                " lessons completed out of " + this.totalLessons
            );
        }
        
        this.isAllLessonsCompleted = true;
        this.overallProgress = Percentage.complete();
        this.allLessonsCompletedAt = LocalDateTime.now();
    }
    
    /**
     * Recalculate overall progress percentage
     * 
     * BUSINESS LOGIC: progress = (completed / total) * 100%
     */
    private void recalculateProgress() {
        if (this.totalLessons <= 0) {
            this.overallProgress = Percentage.zero();
            return;
        }
        
        this.overallProgress = Percentage.fromFraction(
            this.lessonsCompleted, 
            this.totalLessons
        );
    }
    
    // ========== BUSINESS LOGIC QUERIES ==========
    
    /**
     * Check if any progress has been made
     */
    public boolean hasProgress() {
        return this.lessonsCompleted > 0;
    }
    
    /**
     * Get remaining lessons count
     */
    public int getRemainingLessons() {
        return Math.max(0, this.totalLessons - this.lessonsCompleted);
    }
    
    /**
     * Get progress as percentage value (for DTOs)
     */
    public double getProgressPercentage() {
        return this.overallProgress.getValue();
    }
    
    // ========== GUARDS ==========
    
    private static void guardAgainstNull(Object value, String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
    
    private static void guardAgainstNegative(int value, String parameterName) {
        if (value < 0) {
            throw new IllegalArgumentException(parameterName + " cannot be negative: " + value);
        }
    }
}

