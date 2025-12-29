package com.se347.analysticservice.entities.events.instructor;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when instructor has been inactive for a certain period.
 * This can trigger re-engagement campaigns or reminders.
 */
@Value
public class InstructorInactivityDetectedEvent implements DomainEvent {
    
    UUID eventId;
    UUID instructorStatsId;
    UUID instructorId;
    LocalDateTime lastCourseCreatedAt;
    Integer daysSinceLastActivity;
    LocalDateTime occurredAt;
    
    public static InstructorInactivityDetectedEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        LocalDateTime lastCourseCreatedAt,
        Integer daysSinceLastActivity
    ) {
        return new InstructorInactivityDetectedEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            lastCourseCreatedAt,
            daysSinceLastActivity,
            LocalDateTime.now()
        );
    }
}

