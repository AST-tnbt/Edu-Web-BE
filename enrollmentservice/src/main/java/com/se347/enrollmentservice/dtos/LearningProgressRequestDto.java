package com.se347.enrollmentservice.dtos;

import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgressRequestDto {
    private UUID learningProgressId;
    private UUID enrollmentId;
    private UUID lessonId;
    private boolean isCompleted;
}
