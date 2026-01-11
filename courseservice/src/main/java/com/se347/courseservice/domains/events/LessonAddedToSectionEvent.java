package com.se347.courseservice.domains.events;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class LessonAddedToSectionEvent implements DomainEvent {
    UUID eventId;
    UUID sectionId;
    UUID lessonId;
    String lessonTitle;
    LocalDateTime occurredAt;
    
    public static LessonAddedToSectionEvent from(
        UUID sectionId,
        UUID lessonId,
        String lessonTitle
    ) {
        return new LessonAddedToSectionEvent(
            UUID.randomUUID(),
            sectionId,
            lessonId,
            lessonTitle,
            LocalDateTime.now()
        );
    }
}