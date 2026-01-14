package com.se347.analysticservice.dtos.responses.instructor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorDailyStatsResponseDto {
    private LocalDate date;
    private Long newEnrollments;
    private Long activeStudents;
    private BigDecimal dailyRevenue;
    private Long courseCompletions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
