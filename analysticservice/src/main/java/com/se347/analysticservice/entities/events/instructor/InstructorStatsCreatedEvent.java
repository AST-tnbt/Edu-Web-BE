package com.se347.analysticservice.entities.events.instructor;

import com.se347.analysticservice.entities.events.DomainEvent;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when instructor stats are created.
 */
@Value
public class InstructorStatsCreatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID instructorStatsId;
    UUID instructorId;
    Long totalCourses;
    Long totalStudents;
    LocalDateTime occurredAt;
    
    public static InstructorStatsCreatedEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        Long totalCourses,
        Long totalStudents
    ) {
        return new InstructorStatsCreatedEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            totalCourses,
            totalStudents,
            LocalDateTime.now()
        );
    }
}
