package com.se347.enrollmentservice.dtos;

import java.util.UUID;
import java.time.LocalDateTime;
import lombok.*;
import java.lang.Integer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgressResponseDto {
    private UUID courseProgressId;
    private UUID enrollmentId;
    private double overallProgress;
    private Integer lessonsCompleted;
    private Integer totalLessons;
    private boolean isCourseCompleted;
    private LocalDateTime courseCompletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;  
}
