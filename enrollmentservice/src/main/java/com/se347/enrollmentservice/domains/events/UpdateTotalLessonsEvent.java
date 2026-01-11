package com.se347.enrollmentservice.domains.events;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateTotalLessonsEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID enrollmentId;
    private final Integer totalLessons;
    private final LocalDateTime occurredAt;

    public static UpdateTotalLessonsEvent now(UUID enrollmentId, Integer totalLessons) {
        return new UpdateTotalLessonsEvent(
            UUID.randomUUID(),
            enrollmentId,
            totalLessons,
            LocalDateTime.now()
        );
    }
}
