package com.se347.courseservice.domains.events;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class SectionAddedToCourseEvent implements DomainEvent {
    UUID eventId;
    UUID courseId;
    UUID sectionId;
    String sectionTitle;
    LocalDateTime occurredAt;
    
    public static SectionAddedToCourseEvent from(
        UUID courseId, 
        UUID sectionId, 
        String sectionTitle
    ) {
        return new SectionAddedToCourseEvent(
            UUID.randomUUID(),
            courseId,
            sectionId,
            sectionTitle,
            LocalDateTime.now()
        );
    }
}