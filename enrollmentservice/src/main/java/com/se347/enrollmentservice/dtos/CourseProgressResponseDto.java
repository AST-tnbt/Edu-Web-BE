package com.se347.enrollmentservice.dtos;

import java.util.UUID;
import java.time.LocalDateTime;
import lombok.*;
import java.lang.Integer;
import com.se347.enrollmentservice.entities.valueobjects.Percentage;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgressResponseDto {
    private UUID courseProgressId;
    private Percentage overallProgress;
    private Integer lessonsCompleted;
    private Integer totalLessons;
    private boolean isAllLessonsCompleted;
    private LocalDateTime allLessonsCompletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;  
}
