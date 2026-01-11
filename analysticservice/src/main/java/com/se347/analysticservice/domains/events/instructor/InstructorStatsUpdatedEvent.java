package com.se347.analysticservice.domains.events.instructor;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

import com.se347.analysticservice.domains.events.DomainEvent;

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
    Double averageCompletionRate;
    LocalDateTime occurredAt;
    
    public static InstructorStatsUpdatedEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        Long totalCourses,
        Long totalStudents,
        Double averageCompletionRate
    ) {
        return new InstructorStatsUpdatedEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            totalCourses,
            totalStudents,
            averageCompletionRate,
            LocalDateTime.now()
        );
    }
}

