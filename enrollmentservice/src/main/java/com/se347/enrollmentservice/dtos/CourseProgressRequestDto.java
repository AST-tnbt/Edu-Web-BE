package com.se347.enrollmentservice.dtos;

import java.util.UUID;
import lombok.*;
import java.lang.Integer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgressRequestDto {
    private UUID courseProgressId;
    private Integer lessonsCompleted;
    private Integer totalLessons;
}
