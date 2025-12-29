package com.se347.analysticservice.dtos.events.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO received when a student's course progress is updated.
 * Used for tracking engagement and calculating average completion rates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgressUpdatedEvent {
    
    /**
     * Unique event identifier.
     */
    private UUID eventId;
    
    /**
     * ID of the student.
     */
    private UUID studentId;
    
    /**
     * ID of the course.
     */
    private UUID courseId;
    
    /**
     * ID of the instructor who owns the course.
     */
    private UUID instructorId;
    
    /**
     * Enrollment ID.
     */
    private UUID enrollmentId;
    
    /**
     * Previous completion rate.
     */
    private Double previousCompletionRate;
    
    /**
     * Current completion rate (0.0 - 100.0).
     */
    private Double currentCompletionRate;
    
    /**
     * ID of the lesson/module that was completed (optional).
     */
    private UUID completedLessonId;
    
    /**
     * Timestamp when progress was updated.
     */
    private LocalDateTime updatedAt;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
    
    /**
     * Event version for compatibility.
     */
    private Integer version;
}

