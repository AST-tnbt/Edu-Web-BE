package com.se347.enrollmentservice.domains.events;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EnrollmentCompletedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID enrollmentId;
    private final UUID courseId;
    private final UUID studentId;
    private final UUID instructorId;
    private final LocalDateTime occurredAt;

    public static EnrollmentCompletedEvent now(UUID enrollmentId, UUID courseId, UUID studentId, UUID instructorId, LocalDateTime occurredAt) {
        return new EnrollmentCompletedEvent(
            UUID.randomUUID(),
            enrollmentId,
            courseId,
            studentId,
            instructorId,
            occurredAt
        );
    }
}
