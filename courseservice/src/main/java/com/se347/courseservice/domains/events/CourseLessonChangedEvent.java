package com.se347.courseservice.domains.events;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class CourseLessonChangedEvent implements DomainEvent {
    UUID eventId;
    UUID courseId;
    int totalLessons;
    LocalDateTime occurredAt;

    public static CourseLessonChangedEvent from(UUID courseId, int totalLessons) {
        return new CourseLessonChangedEvent(
            UUID.randomUUID(),
            courseId,
            totalLessons,
            LocalDateTime.now()
        );
    }
}
