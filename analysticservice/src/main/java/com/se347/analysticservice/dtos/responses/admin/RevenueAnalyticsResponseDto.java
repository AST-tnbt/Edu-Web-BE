package com.se347.analysticservice.dtos.responses.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueAnalyticsResponseDto {
    private LocalDate date;
    private BigDecimal totalRevenue;
    private Long totalTransactions;
}
