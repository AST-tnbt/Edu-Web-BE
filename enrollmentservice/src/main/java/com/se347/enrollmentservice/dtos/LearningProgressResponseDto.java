package com.se347.enrollmentservice.dtos;

import java.util.UUID;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgressResponseDto {
    private UUID learningProgressId;
    private UUID enrollmentId;
    private UUID contentId;
    private UUID lessonId;
    private double progressPercentage;
    private int timeSpent;
    private boolean isCompleted;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime completedAt;
}