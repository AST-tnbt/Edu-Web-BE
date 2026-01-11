package com.se347.analysticservice.domains.events.instructor;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

import com.se347.analysticservice.domains.events.DomainEvent;

/**
 * Event published when instructor creates a new course.
 * This increments their course count and can trigger achievement notifications.
 */
@Value
public class CourseAddedToInstructorEvent implements DomainEvent {
    UUID eventId;
    UUID instructorStatsId;
    UUID instructorId;
    UUID courseId;
    Long newTotalCourses;
    LocalDateTime occurredAt;
    
    public static CourseAddedToInstructorEvent now(
        UUID instructorStatsId,
        UUID instructorId,
        UUID courseId,
        Long newTotalCourses) {
        return new CourseAddedToInstructorEvent(
            UUID.randomUUID(),
            instructorStatsId,
            instructorId,
            courseId,
            newTotalCourses,
            LocalDateTime.now()
        );
    }
}

