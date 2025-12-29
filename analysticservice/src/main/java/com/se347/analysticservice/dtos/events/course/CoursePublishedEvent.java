package com.se347.analysticservice.dtos.events.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO received from Course Service when a course is published.
 * Published courses become available for enrollment and contribute to active course metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePublishedEvent {
    
    /**
     * Unique event identifier.
     */
    private UUID eventId;
    
    /**
     * ID of the published course.
     */
    private UUID courseId;
    
    /**
     * ID of the instructor who owns the course.
     */
    private UUID instructorId;
    
    /**
     * Course title.
     */
    private String title;
    
    /**
     * Timestamp when the course was published.
     */
    private LocalDateTime publishedAt;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
    
    /**
     * Event version for compatibility.
     */
    private Integer version;
}

