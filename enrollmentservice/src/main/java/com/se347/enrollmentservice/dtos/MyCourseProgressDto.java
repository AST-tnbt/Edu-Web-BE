package com.se347.enrollmentservice.dtos;

import lombok.*;

import java.util.UUID;
import java.time.LocalDateTime;
import com.se347.enrollmentservice.enums.EnrollmentStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyCourseProgressDto {
    private UUID courseId;
    private String courseSlug;
    private Integer lessonsCompleted;
    private Integer totalLessons;
    private Double overallProgress;
    private boolean courseCompleted;
    private EnrollmentStatus enrollmentStatus;
    private LocalDateTime enrolledAt;
}
