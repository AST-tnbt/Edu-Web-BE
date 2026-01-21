package com.se347.analysticservice.controllers.admin;

import com.se347.analysticservice.dtos.responses.admin.InstructorStatsResponseDto;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.services.admin.AdminInstructorAnalyticsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<InstructorStatsResponseDto>> getTopInstructorsByRevenue(
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<InstructorStatsResponseDto> stats = instructorAnalyticsService
            .getTopInstructorsByRevenue(limit)
            .stream()
            .map(this::toStatsDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(stats);
    }

    private InstructorStatsResponseDto toStatsDto(InstructorOverview overview) {
        return InstructorStatsResponseDto.builder()
            .instructorId(overview.getInstructorId())
            .totalCourses(overview.getTotalCourses().getValue())
            .totalStudents(overview.getTotalStudents().getValue())
            .totalRevenue(overview.getTotalRevenue().getAmount())
            .averageCompletionRate(overview.getAverageCompletionRate() != null 
                ? overview.getAverageCompletionRate().getValue() 
                : 0.0)
            .build();
    }
}
