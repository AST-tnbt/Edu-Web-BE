package com.se347.enrollmentservice.dtos;

import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgressRequestDto {
    private UUID enrollmentId;
    private UUID contentId;
    private UUID lessonId;
    private double progressPercentage;
    private int timeSpent;
    private boolean isCompleted;
}
