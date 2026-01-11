package com.se347.enrollmentservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.se347.enrollmentservice.services.CourseProgressQueryService;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/course-progress")
@RequiredArgsConstructor
public class CourseProgressController {
    
    private final CourseProgressQueryService courseProgressService;

    @GetMapping("/id/{courseProgressId}")
    public ResponseEntity<CourseProgressResponseDto> getCourseProgressById(
            @PathVariable UUID courseProgressId) {
        return ResponseEntity.ok(courseProgressService.getCourseProgressById(courseProgressId));
    }
    
    @GetMapping("/enrollment/id/{enrollmentId}")
    public ResponseEntity<CourseProgressResponseDto> getCourseProgressByEnrollmentId(
        @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(courseProgressService.getCourseProgressByEnrollmentId(enrollmentId));
    }
}
