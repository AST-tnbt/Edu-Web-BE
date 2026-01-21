package com.se347.analysticservice.dtos.responses.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformOverviewResponseDto {
    private UUID overviewId;
    private Long totalUsers;
    private Long totalEnrollments;
    private BigDecimal totalRevenue;
    private BigDecimal revenueByPeriod;
    private Long newUsersCount;
    private Long newEnrollmentsCount;
    private Double averageCompletionRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
