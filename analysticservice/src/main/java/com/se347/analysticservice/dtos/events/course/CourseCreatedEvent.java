package com.se347.analysticservice.dtos.events.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO received from Course Service when a new course is created.
 * This is an external event from Course bounded context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreatedEvent {
    
    /**
     * Unique event identifier.
     */
    private UUID eventId;
    
    /**
     * ID of the newly created course.
     */
    private UUID courseId;
    
    /**
     * Course title.
     */
    private String courseTitle;
    
    /**
     * Course slug.
     */
    private String courseSlug;

    /**
     * ID of the instructor who created the course.
     */
    private UUID instructorId;

    /**
     * Course price (for revenue tracking).
     */
    private BigDecimal price;
    
    /**
     * Currency code (e.g., USD, VND).
     */
    private String categoryName;
    
    /**
     * Event occurrence timestamp.
     */
    private LocalDateTime occurredAt;
}

