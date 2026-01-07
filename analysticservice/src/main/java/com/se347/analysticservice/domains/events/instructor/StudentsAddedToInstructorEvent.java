package com.se347.analysticservice.domains.events.instructor;

import lombok.Value;
import java.util.UUID;

import com.se347.analysticservice.domains.events.DomainEvent;

import java.time.LocalDateTime;

/**
 * Event published when students enroll in instructor's courses.
 * This updates the total student count and can trigger milestone notifications.
 */
@Value
public class StudentsAddedToInstructorEvent implements DomainEvent {
    
    UUID eventId;
    UUID instructorStatsId;
    UUID instructorId;
    Long studentsAdded;
    Long newTotalStudents;
    LocalDateTime occurredAt;
    
    public static StudentsAddedToInstructorEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        Long studentsAdded,
        Long newTotalStudents
    ) {
        return new StudentsAddedToInstructorEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            studentsAdded,
            newTotalStudents,
            LocalDateTime.now()
        );
    }
}

