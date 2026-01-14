package com.se347.analysticservice.controllers.admin;

import com.se347.analysticservice.dtos.responses.admin.PlatformOverviewResponseDto;
import com.se347.analysticservice.entities.admin.platform.PlatformOverview;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.services.admin.PlatformOverviewService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics/admin/platform")
@RequiredArgsConstructor
public class AdminPlatformAnalyticsController {

    private final PlatformOverviewService platformOverviewService;

    @GetMapping("/overview/latest")
    public ResponseEntity<PlatformOverviewResponseDto> getLatestOverview(
        @RequestParam Period period
    ) {
        PlatformOverview overview = platformOverviewService.getLatestOverview(period);
        return ResponseEntity.ok(toOverviewDto(overview));
    }

    @GetMapping("/overview")
    public ResponseEntity<PlatformOverviewResponseDto> getOverviewForPeriod(
        @RequestParam Period period,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return platformOverviewService.getOverviewForPeriod(period, startDate, endDate)
            .map(this::toOverviewDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/overview/history")
    public ResponseEntity<List<PlatformOverviewResponseDto>> getOverviewHistory(
        @RequestParam Period period,
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<PlatformOverviewResponseDto> overviews = platformOverviewService
            .getOverviewHistory(period, limit)
            .stream()
            .map(this::toOverviewDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(overviews);
    }

    @PostMapping("/overview/generate")
    public ResponseEntity<PlatformOverviewResponseDto> generatePlatformOverview(
        @RequestParam Period period,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        PlatformOverview overview = platformOverviewService
            .generatePlatformOverview(period, startDate, endDate);
        return ResponseEntity.ok(toOverviewDto(overview));
    }

    @PostMapping("/overview/initialize")
    public ResponseEntity<PlatformOverviewResponseDto> initializeCurrentPeriodOverview(
        @RequestParam Period period
    ) {
        PlatformOverview overview = platformOverviewService.initializeCurrentPeriodOverview(period);
        return ResponseEntity.ok(toOverviewDto(overview));
    }

    private PlatformOverviewResponseDto toOverviewDto(PlatformOverview overview) {
        return PlatformOverviewResponseDto.builder()
            .overviewId(overview.getPlatformOverviewId())
            .totalUsers(overview.getTotalUsers().getValue())
            .totalCourses(overview.getTotalActiveCourses().getValue())
            .totalEnrollments(overview.getTotalEnrollments().getValue())
            .totalRevenue(overview.getTotalRevenue().getAmount())
            .averageCompletionRate(overview.getAverageCompletionRate() != null 
                ? overview.getAverageCompletionRate().getValue() 
                : 0.0)
            .startDate(overview.getStartDate())
            .endDate(overview.getEndDate())
            .createdAt(overview.getCreatedAt())
            .updatedAt(overview.getUpdatedAt())
            .build();
    }
}
