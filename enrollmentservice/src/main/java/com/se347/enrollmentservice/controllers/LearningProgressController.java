package com.se347.enrollmentservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.se347.enrollmentservice.services.LearningProgressQueryService;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;
import com.se347.enrollmentservice.services.EnrollmentCommandService;

import lombok.RequiredArgsConstructor;
import java.util.UUID;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/learning-progress")
@RestController
public class LearningProgressController {
    private final EnrollmentCommandService enrollmentCommandService;
    private final LearningProgressQueryService learningProgressQueryService;

    @GetMapping("/id/{learningProgressId}")
    public ResponseEntity<LearningProgressResponseDto> getLearningProgressById(
            @PathVariable UUID learningProgressId, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(learningProgressQueryService.getLearningProgressById(learningProgressId, userId));
    }

    @GetMapping("/enrollment/id/{enrollmentId}")
    public ResponseEntity<List<LearningProgressResponseDto>> getLearningProgressByEnrollmentId(
            @PathVariable UUID enrollmentId, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(learningProgressQueryService.getLearningProgressByEnrollmentId(enrollmentId, userId));
    } 

    @GetMapping("/lesson/id/{lessonId}/enrollment/id/{enrollmentId}")
    public ResponseEntity<LearningProgressResponseDto> getLearningProgressByLessonIdAndEnrollmentId(
        @PathVariable UUID lessonId, 
        @PathVariable UUID enrollmentId,
        @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(enrollmentCommandService.getLearningProgressByEnrollmentIdAndLessonId(enrollmentId, lessonId, userId));
    }
    
    @PostMapping("/lesson/id/{lessonId}/enrollment/id/{enrollmentId}/complete")
    public ResponseEntity<LearningProgressResponseDto> markAsCompleted(@PathVariable UUID lessonId, @PathVariable UUID enrollmentId, @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(enrollmentCommandService.markLessonAsCompleted(lessonId, enrollmentId, userId));
    }
}
