package com.se347.analysticservice.dtos.events.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO received when a student completes a course (reaches 100% completion).
 * Used for calculating average completion rate and course effectiveness metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCompletedEvent {
    
    /**
     * Unique event identifier.
     */
    private UUID eventId;
    
    /**
     * ID of the student who completed the course.
     */
    private UUID studentId;
    
    /**
     * ID of the completed course.
     */
    private UUID courseId;
    
    /**
     * ID of the instructor who owns the course.
     */
    private UUID instructorId;
    
    /**
     * Enrollment ID associated with this completion.
     */
    private UUID enrollmentId;
    
    /**
     * Completion rate (should be 100.0 for this event).
     */
    private Double completionRate;
    
    /**
     * Total time spent on the course (in minutes).
     */
    private Long totalTimeSpentMinutes;
    
    /**
     * Timestamp when the course was completed.
     */
    private LocalDateTime completedAt;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
    
    /**
     * Event version for compatibility.
     */
    private Integer version;
}

