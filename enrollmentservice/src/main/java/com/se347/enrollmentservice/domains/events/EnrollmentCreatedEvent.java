package com.se347.enrollmentservice.domains.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: Enrollment was created
 * 
 * WHEN RAISED:
 * - Student successfully enrolls in a course
 * 
 * WHO LISTENS:
 * - NotificationService: Send welcome email
 * - AnalyticsService: Track enrollment metrics
 * - CourseService: Update enrollment count
 */
@Getter
@RequiredArgsConstructor
public class EnrollmentCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final UUID enrollmentId;
    private final UUID courseId;
    private final UUID studentId;
    private final UUID instructorId;
    private final LocalDateTime enrolledAt;
    private final LocalDateTime occurredAt;
    
    public static EnrollmentCreatedEvent now(
            UUID enrollmentId, 
            UUID courseId, 
            UUID studentId,
            UUID instructorId,
            LocalDateTime enrolledAt) {
        return new EnrollmentCreatedEvent(
                UUID.randomUUID(),
                enrollmentId, 
                courseId, 
                studentId, 
                instructorId,
                enrolledAt,
                LocalDateTime.now());
    }
}

