package com.se347.analysticservice.entities.events.instructor;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when instructor stats are updated.
 */
@Value
public class InstructorStatsUpdatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID instructorStatsId;
    UUID instructorId;
    Long totalCourses;
    Long totalStudents;
    LocalDateTime occurredAt;
    
    public static InstructorStatsUpdatedEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        Long totalCourses,
        Long totalStudents
    ) {
        return new InstructorStatsUpdatedEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            totalCourses,
            totalStudents,
            LocalDateTime.now()
        );
    }
}

