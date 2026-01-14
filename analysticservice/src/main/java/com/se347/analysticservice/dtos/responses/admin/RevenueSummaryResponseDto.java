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
public class RevenueSummaryResponseDto {
    private BigDecimal totalRevenue;
    private BigDecimal averageDailyRevenue;
    private Long totalTransactions;
    private LocalDate highestRevenueDay;
    private BigDecimal highestRevenueAmount;
}
