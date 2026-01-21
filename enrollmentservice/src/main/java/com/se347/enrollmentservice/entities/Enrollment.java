package com.se347.enrollmentservice.entities;

import com.se347.enrollmentservice.domains.events.*;
import com.se347.enrollmentservice.domains.events.EnrollmentCreatedEvent;
import com.se347.enrollmentservice.domains.events.EnrollmentCompletedEvent;
import com.se347.enrollmentservice.domains.events.UpdateTotalLessonsEvent;
import com.se347.enrollmentservice.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "enrollments",
       uniqueConstraints = @UniqueConstraint(name = "uk_student_course", columnNames = {"studentId", "courseId"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA only
public class Enrollment extends AbstractAggregateRoot {
    
    @Id
    private UUID enrollmentId;

    @Column(nullable = false)
    private UUID courseId;

    @Column(nullable = false)
    private String courseSlug;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID instructorId;

    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus enrollmentStatus;

    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CourseProgress courseProgress;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LearningProgress> learningProgresses = new ArrayList<>();

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

    // ========== FACTORY METHOD ==========
    
    /**
     * Enroll a student in a course
     * 
     * BUSINESS RULES:
     * - Creates enrollment in ACTIVE status
     * - Sets payment status based on course price
     * - Initializes course progress with 0% completion
     * - Raises EnrollmentCreatedEvent
     * 
     * @param courseId The course UUID
     * @param courseSlug The course slug for easy lookup
     * @param studentId The student UUID
     * @param totalLessons Total number of lessons in the course
     * @param instructorId The instructor UUID
     * @return New enrollment
     */
    public static Enrollment enroll(
        UUID courseId, 
        String courseSlug,
        UUID studentId, 
        UUID instructorId,
        int totalLessons
    ) {
        // Guards
        guardAgainstNull(courseId, "Course ID");
        guardAgainstNullOrEmpty(courseSlug, "Course slug");
        guardAgainstNull(studentId, "Student ID");
        guardAgainstNegative(totalLessons, "Total lessons");
        
        Enrollment enrollment = new Enrollment();
        enrollment.enrollmentId = UUID.randomUUID();
        enrollment.courseId = courseId;
        enrollment.courseSlug = courseSlug;
        enrollment.studentId = studentId;
        enrollment.instructorId = instructorId;
        enrollment.enrolledAt = LocalDateTime.now();
        enrollment.enrollmentStatus = EnrollmentStatus.ACTIVE;
        
        // Initialize course progress
        CourseProgress progress = CourseProgress.createFor(enrollment, totalLessons);
        enrollment.courseProgress = progress;
        
        // Register domain event
        enrollment.registerEvent(
            EnrollmentCreatedEvent.now(enrollment.enrollmentId, courseId, studentId, instructorId, enrollment.enrolledAt) // enrollmentId set after save
        );
        
        return enrollment;
    }

    // ========== DOMAIN METHODS (Lesson Progress) ==========
    
    /**
     * Mark a lesson as completed
     * 
     * BUSINESS RULES:
     * - Enrollment must be ACTIVE
     * - Lesson can only be completed once (idempotent)
     * - Updates course progress
     * - Raises LessonCompletedEvent
     * - May raise CourseCompletedEvent if all lessons done
     * 
     * @param lessonId The lesson UUID
     */
    public LearningProgress markLessonAsCompleted(UUID lessonId) {
        guardAgainstNull(lessonId, "Lesson ID");
        ensureEnrollmentIsActive();
        
        // Find or create learning progress for this lesson
        LearningProgress progress = findOrCreateLearningProgress(lessonId);
        
        // If already completed, no-op (idempotent)
        if (progress.isCompleted()) {
            return progress;
        }
        
        // Mark as completed
        progress.markAsCompleted();
        
        // Update overall course progress
        courseProgress.incrementCompletedLessons();
        
        // Register event for overall progress update (to be published to RabbitMQ)
        registerEvent(courseProgress.createUpdateOverallProgressEvent());
        
        // Check if course is now completed
        if (courseProgress.isAllLessonsCompleted() && this.enrollmentStatus != EnrollmentStatus.COMPLETED) {
            this.completeEnrollment();
        }

        return progress;
    }
    
    /**
     * Record that student accessed a lesson
     * 
     * Updates lastAccessedAt timestamp
     */
    public LearningProgress recordLessonAccess(UUID lessonId) {
        guardAgainstNull(lessonId, "Lesson ID");
        
        LearningProgress progress = findOrCreateLearningProgress(lessonId);
        progress.recordAccess();
        return progress;
    }

    // ========== DOMAIN METHODS (Enrollment Status) ==========
    
    /**
     * Suspend enrollment (e.g., for policy violations)
     * 
     * BUSINESS RULE: Cannot suspend if already completed
     */
    public void suspend() {
        if (this.enrollmentStatus == EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot suspend completed enrollment");
        }
        
        this.enrollmentStatus = EnrollmentStatus.SUSPENDED;
    }
    
    /**
     * Cancel enrollment (student requested or payment failed)
     * 
     * BUSINESS RULE: Cannot cancel if already completed
     */
    public void cancel() {
        if (this.enrollmentStatus == EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed enrollment");
        }
        
        this.enrollmentStatus = EnrollmentStatus.CANCELLED;
    }
    
    /**
     * Reactivate suspended enrollment
     * 
     * BUSINESS RULE: Can only reactivate from SUSPENDED status
     */
    public void reactivate() {
        if (this.enrollmentStatus != EnrollmentStatus.SUSPENDED) {
            throw new IllegalStateException("Can only reactivate suspended enrollments");
        }
        
        this.enrollmentStatus = EnrollmentStatus.ACTIVE;
    }
    
    /**
     * Complete enrollment (when all lessons finished)
     * 
     * BUSINESS RULE: Automatically called when course progress reaches 100%
     */
    private void completeEnrollment() {
        this.enrollmentStatus = EnrollmentStatus.COMPLETED;
        
        // Raise domain event
        registerEvent(EnrollmentCompletedEvent.now(
            this.enrollmentId,
            this.courseId,
            this.studentId,
            this.instructorId,
            this.courseProgress.getAllLessonsCompletedAt()
        ));
    }

    // ========== DOMAIN METHODS (Course Structure Updates) ==========
    
    /**
     * Update total lessons count (when course structure changes)
     * 
     * Called when course adds/removes lessons
     */
    public CourseProgress updateTotalLessons(int newTotal) {
        guardAgainstNegative(newTotal, "Total lessons");
        
        this.courseProgress.updateTotalLessons(newTotal);
        registerEvent(UpdateTotalLessonsEvent.now(this.enrollmentId, newTotal));

        return this.courseProgress;
    }

    // ========== BUSINESS LOGIC QUERIES ==========
    
    /**
     * Check if enrollment is active
     */
    public boolean canAccessCourse() {
        return this.enrollmentStatus == EnrollmentStatus.ACTIVE || this.enrollmentStatus == EnrollmentStatus.COMPLETED;
    }
    
    /**
     * Check if student has completed a specific lesson
     */
    public boolean hasCompletedLesson(UUID lessonId) {
        return learningProgresses.stream()
            .anyMatch(p -> p.isForLesson(lessonId) && p.isCompleted());
    }
    
    /**
     * Get overall progress percentage
     */
    public double getProgressPercentage() {
        return courseProgress.getProgressPercentage();
    }

    public LearningProgress getOrCreateLessonProgress(UUID lessonId) {
        return findOrCreateLearningProgress(lessonId);
    }

    // ========== PRIVATE HELPERS ==========
    
    /**
     * Find existing learning progress or create new one
     */
    private LearningProgress findOrCreateLearningProgress(UUID lessonId) {
        Optional<LearningProgress> existing = learningProgresses.stream()
            .filter(p -> p.isForLesson(lessonId))
            .findFirst();
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new progress entry
        LearningProgress newProgress = LearningProgress.createFor(this, lessonId);
        learningProgresses.add(newProgress);
        return newProgress;
    }
    
    /**
     * Ensure enrollment is in ACTIVE status
     */
    private void ensureEnrollmentIsActive() {
        if (this.enrollmentStatus != EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException(
                "Cannot perform this operation on non-active enrollment. Status: " + this.enrollmentStatus
            );
        }
    }

    public void updateEnrollmentStatus(EnrollmentStatus newStatus) {
        this.enrollmentStatus = newStatus;
        this.updatedAt = LocalDateTime.now();
        registerEvent(EnrollmentStatusUpdatedEvent.now(this.enrollmentId, this.enrollmentStatus));
    }
    // ========== GUARDS ==========
    
    private static void guardAgainstNull(Object value, String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
    
    private static void guardAgainstNullOrEmpty(String value, String parameterName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be null or empty");
        }
    }
    
    private static void guardAgainstNegative(int value, String parameterName) {
        if (value < 0) {
            throw new IllegalArgumentException(parameterName + " cannot be negative: " + value);
        }
    }
}

