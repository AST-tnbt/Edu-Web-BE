package com.se347.analysticservice.dtos.events.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO received when a student enrolls in a course.
 * This event is critical for tracking enrollment growth and course popularity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentCreatedEvent {
    
    /**
     * Unique event identifier.
     */
    private UUID eventId;
    
    /**
     * ID of the enrollment record.
     */
    private UUID enrollmentId;
    
    /**
     * ID of the student who enrolled.
     */
    private UUID studentId;
    
    /**
     * ID of the course being enrolled in.
     */
    private UUID courseId;
    
    /**
     * ID of the instructor who owns the course.
     */
    private UUID instructorId;
    
    /**
     * Course title (for analytics).
     */
    private String courseTitle;
    
    /**
     * Enrollment type (FREE, PAID).
     */
    private String enrollmentType;
    
    /**
     * Timestamp when enrollment was created.
     */
    private LocalDateTime enrolledAt;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
    
    /**
     * Event version for compatibility.
     */
    private Integer version;
}

