package com.se347.enrollmentservice.domains.events;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.se347.enrollmentservice.enums.EnrollmentStatus;

@Getter
@RequiredArgsConstructor
public class EnrollmentStatusUpdatedEvent implements DomainEvent{
    private final UUID eventId;
    private final UUID enrollmentId;
    private final EnrollmentStatus enrollmentStatus;
    private final LocalDateTime occurredAt;

    public static EnrollmentStatusUpdatedEvent now(UUID enrollmentId, EnrollmentStatus enrollmentStatus) {
        return new EnrollmentStatusUpdatedEvent(
                UUID.randomUUID(), 
                enrollmentId, 
                enrollmentStatus, 
                LocalDateTime.now());
    }
}
