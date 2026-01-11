package com.se347.enrollmentservice.domains.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: Student completed entire course
 * 
 * WHEN RAISED:
 * - All lessons in the course are completed
 * 
 * WHO LISTENS:
 * - CertificateService: Generate certificate
 * - NotificationService: Send congratulations
 * - AnalyticsService: Track completion metrics
 * - GamificationService: Award big reward
 */
@Getter
@RequiredArgsConstructor
public class CourseCompletedEvent implements DomainEvent {

    private final UUID eventId;
    private final UUID enrollmentId;
    private final UUID courseId;
    private final UUID studentId;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredAt;

    public static CourseCompletedEvent now(
            UUID enrollmentId,
            UUID courseId,
            UUID studentId,
            LocalDateTime completedAt
    ) {
        return new CourseCompletedEvent(
                UUID.randomUUID(),        // eventId
                enrollmentId,
                courseId,
                studentId,
                completedAt,
                LocalDateTime.now()       // occurredAt
        );
    }
}


