package com.se347.enrollmentservice.domains.events;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateOverallProgressEvent {

    private final UUID eventId;
    private final UUID enrollmentId;
    private final UUID courseId;
    private final UUID studentId;
    private final UUID instructorId;
    private final double newOverallProgress;
    private final LocalDateTime updatedAt;

    public static UpdateOverallProgressEvent now(
        UUID enrollmentId,
        UUID courseId,
        UUID studentId,
        UUID instructorId,
        double newOverallProgress
    ) {
        return new UpdateOverallProgressEvent(
            UUID.randomUUID(), 
            enrollmentId, 
            courseId, 
            studentId, 
            instructorId, 
            newOverallProgress,
            LocalDateTime.now()
        );
    }
}
