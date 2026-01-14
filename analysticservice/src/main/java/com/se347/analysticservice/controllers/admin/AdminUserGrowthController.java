package com.se347.analysticservice.controllers.admin;

import com.se347.analysticservice.dtos.responses.admin.ActiveUsersResponseDto;
import com.se347.analysticservice.dtos.responses.admin.RetentionRateResponseDto;
import com.se347.analysticservice.dtos.responses.admin.UserGrowthAnalyticsResponseDto;
import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.services.admin.UserGrowthAnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics/admin/user-growth")
@RequiredArgsConstructor
public class AdminUserGrowthController {

    private final UserGrowthAnalyticsService userGrowthAnalyticsService;

    @GetMapping("/latest")
    public ResponseEntity<UserGrowthAnalyticsResponseDto> getMostRecent() {
        return ResponseEntity.ok(
            toDto(userGrowthAnalyticsService.getMostRecentAnalytics())
        );
    }

    @GetMapping("/{date}")
    public ResponseEntity<UserGrowthAnalyticsResponseDto> getAnalyticsForDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return userGrowthAnalyticsService.getAnalyticsForDate(date)
            .map(this::toDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/period")
    public ResponseEntity<List<UserGrowthAnalyticsResponseDto>> getAnalyticsForPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<UserGrowthAnalyticsResponseDto> analytics = userGrowthAnalyticsService
            .getAnalyticsForPeriod(startDate, endDate)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/retention/average")
    public ResponseEntity<RetentionRateResponseDto> getAverageRetentionRate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Percentage averageRetention = userGrowthAnalyticsService
            .getAverageRetentionRate(startDate, endDate);
        
        RetentionRateResponseDto response = RetentionRateResponseDto.builder()
            .averageRetentionRate(averageRetention.getValue())
            .startDate(startDate)
            .endDate(endDate)
            .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active-users/{date}")
    public ResponseEntity<ActiveUsersResponseDto> getTotalActiveUsers(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Count activeUsers = userGrowthAnalyticsService.getTotalActiveUsers(date);
        
        ActiveUsersResponseDto response = ActiveUsersResponseDto.builder()
            .date(date)
            .totalActiveUsers(activeUsers.getValue())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{date}/calculate-retention")
    public ResponseEntity<Void> calculateDailyRetention(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        userGrowthAnalyticsService.calculateDailyRetention(date);
        return ResponseEntity.accepted().build();
    }

    private UserGrowthAnalyticsResponseDto toDto(UserGrowthAnalytics analytics) {
        return UserGrowthAnalyticsResponseDto.builder()
            .userGrowthAnalyticsId(analytics.getUserGrowthAnalyticsId())
            .date(analytics.getDate())
            .newUsersCount(analytics.getNewUsersCount().getValue())
            .activeUsersCount(analytics.getActiveUsersCount().getValue())
            .totalUsers(analytics.getTotalUsers().getValue())
            .retentionRate(analytics.getRetentionRate() != null 
                ? analytics.getRetentionRate().getValue() 
                : 0.0)
            .createdAt(analytics.getCreatedAt())
            .updatedAt(analytics.getUpdatedAt())
            .build();
    }
}
