package com.se347.courseservice.domains.events;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class CourseUpdatedEvent implements DomainEvent {
    UUID eventId;
    UUID courseId;
    String newTitle;
    LocalDateTime occurredAt;
    
    public static CourseUpdatedEvent from(UUID courseId, String newTitle) {
        return new CourseUpdatedEvent(
            UUID.randomUUID(),
            courseId,
            newTitle,
            LocalDateTime.now()
        );
    }
}
