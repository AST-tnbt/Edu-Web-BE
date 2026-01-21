package com.se347.analysticservice.dtos.events.enrollment;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentCompletedEvent {
    private UUID eventId;
    private UUID enrollmentId;
    private UUID courseId;
    private UUID studentId;
    private UUID instructorId;
    private LocalDateTime occurredAt;

    public static EnrollmentCompletedEvent now(UUID enrollmentId, UUID courseId, UUID studentId, UUID instructorId) {
        return new EnrollmentCompletedEvent(UUID.randomUUID(), enrollmentId, courseId, studentId, instructorId, LocalDateTime.now());
    }
}
