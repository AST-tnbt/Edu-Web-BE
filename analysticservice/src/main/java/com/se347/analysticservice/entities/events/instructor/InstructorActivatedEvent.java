package com.se347.analysticservice.entities.events.instructor;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Event published when suspended instructor is reactivated.
 * This restores access and sends reactivation notification.
 */
@Value
public class InstructorActivatedEvent implements DomainEvent {
    UUID eventId;
    UUID instructorStatsId;
    UUID instructorId;
    UUID activatedByAdminId;
    LocalDateTime occurredAt;
    
    public static InstructorActivatedEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        UUID activatedByAdminId) {
        return new InstructorActivatedEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            activatedByAdminId,
            LocalDateTime.now()
        );
    }
}

