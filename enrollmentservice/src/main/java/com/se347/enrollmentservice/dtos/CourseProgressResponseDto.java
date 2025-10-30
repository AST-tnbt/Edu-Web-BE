package com.se347.enrollmentservice.dtos;

import java.util.UUID;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgressResponseDto {
    private UUID courseProgressId;
    private UUID enrollmentId;
    private double overallProgress;
    private int lessonsCompleted;
    private int totalLessons;
    private boolean isCourseCompleted;
    private LocalDateTime courseCompletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
