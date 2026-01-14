package com.se347.analysticservice.controllers.admin;

import com.se347.analysticservice.dtos.responses.admin.RevenueAnalyticsResponseDto;
import com.se347.analysticservice.dtos.responses.admin.RevenueSummaryResponseDto;
import com.se347.analysticservice.entities.admin.revenue.DailyRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.services.admin.RevenueAnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics/admin/revenue")
@RequiredArgsConstructor
public class AdminRevenueAnalyticsController {

    private final RevenueAnalyticsService revenueAnalyticsService;

    @GetMapping("/daily/{date}")
    public ResponseEntity<RevenueAnalyticsResponseDto> getDailyRevenue(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return revenueAnalyticsService.getDailyRevenue(date)
            .map(this::toRevenueDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<RevenueAnalyticsResponseDto>> getRevenueHistory(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<RevenueAnalyticsResponseDto> revenues = revenueAnalyticsService
            .getRevenueHistory(startDate, endDate)
            .stream()
            .map(this::toRevenueDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(revenues);
    }

    @GetMapping("/summary")
    public ResponseEntity<RevenueSummaryResponseDto> getRevenueSummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Money totalRevenue = revenueAnalyticsService.getTotalRevenueForPeriod(startDate, endDate);
        Money averageDaily = revenueAnalyticsService.getAverageDailyRevenue(startDate, endDate);
        Count totalTransactions = revenueAnalyticsService.getTotalTransactionsForPeriod(startDate, endDate);
        DailyRevenue highestDay = revenueAnalyticsService.getHighestRevenueDay(startDate, endDate);

        RevenueSummaryResponseDto summary = RevenueSummaryResponseDto.builder()
            .totalRevenue(totalRevenue.getAmount())
            .averageDailyRevenue(averageDaily.getAmount())
            .totalTransactions(totalTransactions.getValue())
            .highestRevenueDay(highestDay != null ? highestDay.getDate() : null)
            .highestRevenueAmount(highestDay != null ? highestDay.getTotalRevenue().getAmount() : null)
            .build();

        return ResponseEntity.ok(summary);
    }

    @PostMapping("/daily/{date}/recalculate")
    public ResponseEntity<Void> recalculateDailyRevenue(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        revenueAnalyticsService.recalculateDailyRevenue(date);
        return ResponseEntity.accepted().build();
    }

    private RevenueAnalyticsResponseDto toRevenueDto(DailyRevenue revenue) {
        return RevenueAnalyticsResponseDto.builder()
            .date(revenue.getDate())
            .totalRevenue(revenue.getTotalRevenue().getAmount())
            .totalTransactions(revenue.getTotalTransactions().getValue())
            .build();
    }
}
