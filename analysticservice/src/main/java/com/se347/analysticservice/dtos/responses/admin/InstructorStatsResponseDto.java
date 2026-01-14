package com.se347.analysticservice.dtos.responses.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorStatsResponseDto {
    private UUID instructorId;
    private Long totalCourses;
    private Long totalStudents;
    private Double averageCompletionRate;
}
