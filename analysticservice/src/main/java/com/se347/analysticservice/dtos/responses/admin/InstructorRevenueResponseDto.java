package com.se347.analysticservice.dtos.responses.admin;

import com.se347.analysticservice.enums.Period;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorRevenueResponseDto {
    private UUID instructorRevenueId;
    private UUID instructorId;
    private Period period;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private Long totalEnrollments;
    private Long totalCourses;
    private List<CourseRevenueSnapshotResponseDto> topPerformingCourses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
