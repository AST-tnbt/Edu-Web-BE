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
public class UpdateOverallProgressEvent {
    private UUID eventId;
    private UUID enrollmentId;
    private UUID courseId;
    private UUID studentId;
    private UUID instructorId;
    private double newOverallProgress;
    private LocalDateTime updatedAt;
}
