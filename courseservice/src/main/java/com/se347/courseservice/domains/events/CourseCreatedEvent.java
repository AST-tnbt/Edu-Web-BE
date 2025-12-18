package com.se347.courseservice.domains.events;

import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event fired when a new course is created
 * 
 * Consumers:
 * - AnalyticsService: track course creation stats
 * - SearchIndexService: index course for search
 * - NotificationService: notify admin
 */
@Value
public class CourseCreatedEvent implements DomainEvent {
    UUID eventId;
    UUID courseId;
    String courseTitle;
    String courseSlug;
    UUID instructorId;
    BigDecimal price;
    String categoryName;
    LocalDateTime occurredAt;
    
    /**
     * Factory method to create event from Course entity
     */
    public static CourseCreatedEvent from(
        UUID courseId,
        String courseTitle,
        String courseSlug,
        UUID instructorId,
        BigDecimal price,
        String categoryName
    ) {
        return new CourseCreatedEvent(
            UUID.randomUUID(),
            courseId,
            courseTitle,
            courseSlug,
            instructorId,
            price,
            categoryName,
            LocalDateTime.now()
        );
    }
}
