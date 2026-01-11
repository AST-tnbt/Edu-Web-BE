package com.se347.enrollmentservice.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity: Learning Progress for a specific lesson
 * 
 * DDD PATTERN: Entity (within Enrollment aggregate)
 * 
 * BELONGS TO: Enrollment aggregate
 * LIFECYCLE: Created/Updated only through Enrollment aggregate root
 * 
 * BUSINESS RULES:
 * - Once completed, cannot be un-completed
 * - completedAt is set automatically when marked as completed
 * - lastAccessedAt updates every time lesson is accessed
 * 
 * IDENTITY:
 * Unique constraint: (enrollmentId, lessonId) - student can't have duplicate progress for same lesson
 */
@Entity
@Table(name = "learning_progress", 
       uniqueConstraints = @UniqueConstraint(name = "uk_lesson_enrollment", columnNames = {"lessonId", "enrollment_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA only
public class LearningProgress {
    
    @Id
    private UUID learningProgressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false)
    private boolean isCompleted;

    @Column(updatable = true)
    private LocalDateTime lastAccessedAt;

    @Column(updatable = true)
    private LocalDateTime completedAt;

    // ========== FACTORY METHOD (Package-private) ==========
    
    /**
     * Create new learning progress for a lesson
     * 
     * PACKAGE-PRIVATE: Can only be called by Enrollment (same package)
     * This enforces that progress is created through aggregate root
     */
    static LearningProgress createFor(Enrollment enrollment, UUID lessonId) {
        guardAgainstNull(enrollment, "Enrollment");
        guardAgainstNull(lessonId, "Lesson ID");
        
        LearningProgress progress = new LearningProgress();
        progress.learningProgressId = UUID.randomUUID();
        progress.enrollment = enrollment;
        progress.lessonId = lessonId;
        progress.isCompleted = false;
        progress.lastAccessedAt = LocalDateTime.now();
        progress.completedAt = null;
        
        return progress;
    }
    // ========== DOMAIN METHODS ==========
    
    /**
     * Mark lesson as completed
     * 
     * BUSINESS RULE: Once completed, stays completed
     */
    public void markAsCompleted() {
        if (this.isCompleted) {
            return; // Already completed, no-op (idempotent)
        }
        
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * Record that student accessed this lesson
     */
    public void recordAccess() {
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    // ========== BUSINESS LOGIC QUERIES ==========
    
    /**
     * Check if this progress belongs to a specific lesson
     */
    public boolean isForLesson(UUID lessonId) {
        return this.lessonId.equals(lessonId);
    }
    
    // ========== GUARDS ==========
    
    private static void guardAgainstNull(Object value, String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
}

