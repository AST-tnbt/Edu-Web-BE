package com.se347.analysticservice.controllers.admin;

import com.se347.analysticservice.dtos.responses.admin.CourseRevenueSnapshotResponseDto;
import com.se347.analysticservice.dtos.responses.admin.InstructorRevenueResponseDto;
import com.se347.analysticservice.dtos.responses.admin.InstructorStatsResponseDto;
import com.se347.analysticservice.entities.admin.instructor.InstructorStats;
import com.se347.analysticservice.entities.admin.revenue.CourseRevenueSnapshot;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.services.admin.AdminInstructorAnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics/admin/instructors")
@RequiredArgsConstructor
public class AdminInstructorAnalyticsController {

    private final AdminInstructorAnalyticsService instructorAnalyticsService;

    @GetMapping("/{instructorId}/stats")
    public ResponseEntity<InstructorStatsResponseDto> getInstructorStats(
        @PathVariable UUID instructorId
    ) {
        return instructorAnalyticsService.getInstructorStats(instructorId)
            .map(this::toStatsDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/top/students")
    public ResponseEntity<List<InstructorStatsResponseDto>> getTopInstructorsByStudents(
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<InstructorStatsResponseDto> stats = instructorAnalyticsService
            .getTopInstructorsByStudents(limit)
            .stream()
            .map(this::toStatsDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top/courses")
    public ResponseEntity<List<InstructorStatsResponseDto>> getTopInstructorsByCourses(
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<InstructorStatsResponseDto> stats = instructorAnalyticsService
            .getTopInstructorsByCourses(limit)
            .stream()
            .map(this::toStatsDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top/revenue")
    public ResponseEntity<List<InstructorRevenueResponseDto>> getTopInstructorsByRevenue(
        @RequestParam Period period,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<InstructorRevenueResponseDto> revenues = instructorAnalyticsService
            .getTopInstructorsByRevenue(period, endDate, limit)
            .stream()
            .map(this::toRevenueDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(revenues);
    }

    @GetMapping("/{instructorId}/revenue")
    public ResponseEntity<InstructorRevenueResponseDto> getInstructorRevenue(
        @PathVariable UUID instructorId,
        @RequestParam Period period,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return instructorAnalyticsService.getInstructorRevenue(instructorId, period, startDate, endDate)
            .map(this::toRevenueDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{instructorId}/revenue/history")
    public ResponseEntity<List<InstructorRevenueResponseDto>> getInstructorRevenueHistory(
        @PathVariable UUID instructorId,
        @RequestParam Period period
    ) {
        List<InstructorRevenueResponseDto> revenues = instructorAnalyticsService
            .getInstructorRevenueHistory(instructorId, period)
            .stream()
            .map(this::toRevenueDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(revenues);
    }

    private InstructorStatsResponseDto toStatsDto(InstructorStats stats) {
        return InstructorStatsResponseDto.builder()
            .instructorId(stats.getInstructorId())
            .totalCourses(stats.getTotalCourses().getValue())
            .totalStudents(stats.getTotalStudents().getValue())
            .averageCompletionRate(stats.getAverageCompletionRate().getValue())
            .build();
    }

    private InstructorRevenueResponseDto toRevenueDto(InstructorRevenue revenue) {
        return InstructorRevenueResponseDto.builder()
            .instructorRevenueId(revenue.getInstructorRevenueId())
            .instructorId(revenue.getInstructorId())
            .period(revenue.getPeriod())
            .startDate(revenue.getStartDate())
            .endDate(revenue.getEndDate())
            .totalRevenue(revenue.getTotalRevenue().getAmount())
            .totalEnrollments(revenue.getTotalEnrollments().getValue())
            .totalCourses(revenue.getTotalCourses().getValue())
            .topPerformingCourses(revenue.getTopPerformingCourses() != null
                ? revenue.getTopPerformingCourses().stream()
                    .map(this::toCourseSnapshotDto)
                    .collect(Collectors.toList())
                : List.of())
            .createdAt(revenue.getCreatedAt())
            .updatedAt(revenue.getUpdatedAt())
            .build();
    }

    private CourseRevenueSnapshotResponseDto toCourseSnapshotDto(CourseRevenueSnapshot snapshot) {
        return CourseRevenueSnapshotResponseDto.builder()
            .courseId(snapshot.getCourseId())
            .courseTitle(snapshot.getCourseTitle())
            .enrollmentCount(snapshot.getEnrollmentCount().getValue())
            .revenue(snapshot.getRevenue().getAmount())
            .build();
    }
}
