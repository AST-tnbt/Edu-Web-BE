package com.se347.analysticservice.entities.events.instructor;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Event published when instructor is suspended by admin.
 * This should trigger immediate access restriction and notification.
 */
@Value
public class InstructorSuspendedEvent implements DomainEvent {
    
    UUID eventId;
    UUID instructorStatsId;
    UUID instructorId;
    String suspensionReason;
    UUID suspendedByAdminId;
    LocalDateTime occurredAt;
    
    public static InstructorSuspendedEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        String suspensionReason,
        UUID suspendedByAdminId
    ) {
        return new InstructorSuspendedEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            suspensionReason,
            suspendedByAdminId,
            LocalDateTime.now()
        );
    }
}

