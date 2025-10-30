package com.se347.enrollmentservice.dtos;

import java.util.UUID;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgressRequestDto {
    private UUID enrollmentId;
    private double overallProgress;
    private int lessonsCompleted;
    private int totalLessons;
    private boolean isCourseCompleted;
    private LocalDateTime courseCompletedAt;    
}
