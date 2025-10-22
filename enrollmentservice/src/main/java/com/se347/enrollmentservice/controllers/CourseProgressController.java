package com.se347.enrollmentservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.se347.enrollmentservice.services.CourseProgressService;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/course-progress")
public class CourseProgressController {
    
    private final CourseProgressService courseProgressService;

    CourseProgressController(CourseProgressService courseProgressService){
        this.courseProgressService = courseProgressService;
    }

    @PostMapping
    public ResponseEntity<CourseProgressResponseDto> createCourseProgress(@RequestBody CourseProgressRequestDto request) {
        return ResponseEntity.ok(courseProgressService.createCourseProgress(request));
    }
    
    @GetMapping("{courseProgressId}")
    public ResponseEntity<CourseProgressResponseDto> getCourseProgressById(@PathVariable UUID courseProgressId) {
        return ResponseEntity.ok(courseProgressService.getCourseProgressById(courseProgressId));
    }
    
    @GetMapping("enrollment/{enrollmentId}")
    public ResponseEntity<CourseProgressResponseDto> getCourseProgressByEnrollmentId(@PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(courseProgressService.getCourseProgressByEnrollmentId(enrollmentId));
    }

    @PutMapping("{courseProgressId}")
    public ResponseEntity<CourseProgressResponseDto> updateCourseProgress(@PathVariable UUID courseProgressId, @RequestBody CourseProgressRequestDto request) {
        return ResponseEntity.ok(courseProgressService.updateCourseProgress(courseProgressId, request));
    }

    // @PostMapping("{courseProgressId}/complete")
    // public ResponseEntity<CourseProgressResponseDto> markCourseProgressAsCompleted(@PathVariable UUID courseProgressId) {
    //     return ResponseEntity.ok(courseProgressService.markCourseProgressAsCompleted(courseProgressId));
    // }

    // @PatchMapping("{courseProgressId}/overall-progress")
    // public ResponseEntity<CourseProgressResponseDto> updateOverallProgress(@PathVariable UUID courseProgressId, @RequestBody CourseProgressRequestDto request) {
    //     return ResponseEntity.ok(courseProgressService.updateOverallProgress(courseProgressId, request));
    // }
}
