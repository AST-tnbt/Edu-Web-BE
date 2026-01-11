package com.se347.courseservice.domains.events;

import com.se347.courseservice.entities.valueobjects.Money;
import com.se347.courseservice.enums.CourseLevel;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class CourseUpdatedEvent implements DomainEvent {
    UUID eventId;
    UUID courseId;
    String newTitle;
    String newDescription;
    String newThumbnailUrl;
    Money newPrice;
    CourseLevel newLevel;
    LocalDateTime occurredAt;
    
    public static CourseUpdatedEvent from(UUID courseId, String newTitle, String newDescription, String newThumbnailUrl, Money newPrice, CourseLevel newLevel) {
        return new CourseUpdatedEvent(
            UUID.randomUUID(),
            courseId,
            newTitle,
            newDescription,
            newThumbnailUrl,
            newPrice,
            newLevel,
            LocalDateTime.now()
        );
    }
}
