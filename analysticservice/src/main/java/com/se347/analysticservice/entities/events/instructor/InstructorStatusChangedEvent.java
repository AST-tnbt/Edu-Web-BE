package com.se347.analysticservice.entities.events.instructor;

import com.se347.analysticservice.entities.events.DomainEvent;
import com.se347.analysticservice.enums.InstructorStatus;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when instructor status changes.
 * This can trigger notifications, access control updates, etc.
 */
@Value
public class InstructorStatusChangedEvent implements DomainEvent {
    
    UUID eventId;
    UUID instructorStatsId;
    UUID instructorId;
    InstructorStatus previousStatus;
    InstructorStatus newStatus;
    String reason;
    LocalDateTime occurredAt;
    
    public static InstructorStatusChangedEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        InstructorStatus previousStatus,
        InstructorStatus newStatus,
        String reason
    ) {
        return new InstructorStatusChangedEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            previousStatus,
            newStatus,
            reason,
            LocalDateTime.now()
        );
    }
}

