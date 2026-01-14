package com.se347.analysticservice.controllers.instructor;

import com.se347.analysticservice.dtos.responses.instructor.InstructorCourseStatsResponseDto;
import com.se347.analysticservice.dtos.responses.instructor.InstructorDailyStatsResponseDto;
import com.se347.analysticservice.dtos.responses.instructor.InstructorOverviewResponseDto;
import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.services.instructor.InstructorCourseStatsService;
import com.se347.analysticservice.services.instructor.InstructorDailyStatsService;
import com.se347.analysticservice.services.instructor.InstructorOverviewService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics/instructor")
@RequiredArgsConstructor
public class InstructorAnalyticsController {

    private final InstructorOverviewService overviewService;
    private final InstructorCourseStatsService courseStatsService;
    private final InstructorDailyStatsService dailyStatsService;

    @GetMapping("/overview")
    public ResponseEntity<InstructorOverviewResponseDto> getOverview(
        @RequestHeader("X-User-Id") UUID instructorId
    ) {
        return overviewService.getByInstructorId(instructorId)
            .map(this::toOverviewDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<InstructorCourseStatsResponseDto>> getAllCourseStats(
        @RequestHeader("X-User-Id") UUID instructorId
    ) {
        List<InstructorCourseStatsResponseDto> stats = courseStatsService.getByInstructorId(instructorId)
            .stream()
            .map(this::toCourseStatsDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<InstructorCourseStatsResponseDto> getCourseStats(
        @RequestHeader("X-User-Id") UUID instructorId,
        @PathVariable UUID courseId
    ) {
        return courseStatsService.getByInstructorIdAndCourseId(instructorId, courseId)
            .map(this::toCourseStatsDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/courses/top-revenue")
    public ResponseEntity<List<InstructorCourseStatsResponseDto>> getTopCoursesByRevenue(
        @RequestHeader("X-User-Id") UUID instructorId
    ) {
        List<InstructorCourseStatsResponseDto> stats = courseStatsService
            .getByInstructorIdOrderByRevenueDesc(instructorId)
            .stream()
            .map(this::toCourseStatsDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/courses/top-completion")
    public ResponseEntity<List<InstructorCourseStatsResponseDto>> getTopCoursesByCompletion(
        @RequestHeader("X-User-Id") UUID instructorId
    ) {
        List<InstructorCourseStatsResponseDto> stats = courseStatsService
            .getByInstructorIdOrderByCompletionRateDesc(instructorId)
            .stream()
            .map(this::toCourseStatsDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/daily")
    public ResponseEntity<List<InstructorDailyStatsResponseDto>> getDailyStats(
        @RequestHeader("X-User-Id") UUID instructorId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<InstructorDailyStatsResponseDto> stats;
        
        if (startDate != null && endDate != null) {
            stats = dailyStatsService.getByInstructorIdAndDateBetween(instructorId, startDate, endDate)
                .stream()
                .map(this::toDailyStatsDto)
                .collect(Collectors.toList());
        } else {
            stats = dailyStatsService.getByInstructorIdOrderByDateDesc(instructorId)
                .stream()
                .map(this::toDailyStatsDto)
                .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<InstructorDailyStatsResponseDto> getDailyStatsByDate(
        @RequestHeader("X-User-Id") UUID instructorId,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return dailyStatsService.getByInstructorIdAndDate(instructorId, date)
            .map(this::toDailyStatsDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/overview/recalculate-completion-rate")
    public ResponseEntity<Void> recalculateCompletionRate(
        @RequestHeader("X-User-Id") UUID instructorId
    ) {
        overviewService.recalculateAverageCompletionRate(instructorId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private InstructorOverviewResponseDto toOverviewDto(InstructorOverview overview) {
        return InstructorOverviewResponseDto.builder()
            .instructorId(overview.getInstructorId())
            .totalCourses(overview.getTotalCourses().getValue())
            .totalStudents(overview.getTotalStudents().getValue())
            .totalRevenue(overview.getTotalRevenue().getAmount())
            .averageCompletionRate(overview.getAverageCompletionRate() != null 
                ? overview.getAverageCompletionRate().getValue() 
                : 0.0)
            .createdAt(overview.getCreatedAt())
            .updatedAt(overview.getUpdatedAt())
            .build();
    }

    private InstructorCourseStatsResponseDto toCourseStatsDto(InstructorCourseStats stats) {
        return InstructorCourseStatsResponseDto.builder()
            .courseId(stats.getCourseId())
            .totalStudents(stats.getTotalStudents().getValue())
            .totalRevenue(stats.getTotalRevenue().getAmount())
            .completionRate(stats.getCompletionRate() != null 
                ? stats.getCompletionRate().getValue() 
                : 0.0)
            .createdAt(stats.getCreatedAt())
            .updatedAt(stats.getUpdatedAt())
            .build();
    }

    private InstructorDailyStatsResponseDto toDailyStatsDto(InstructorDailyStats stats) {
        return InstructorDailyStatsResponseDto.builder()
            .date(stats.getDate())
            .newEnrollments(stats.getNewEnrollments().getValue())
            .activeStudents(stats.getActiveStudents().getValue())
            .dailyRevenue(stats.getDailyRevenue().getAmount())
            .courseCompletions(stats.getCourseCompletions().getValue())
            .createdAt(stats.getCreatedAt())
            .updatedAt(stats.getUpdatedAt())
            .build();
    }
}
