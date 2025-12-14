package com.se347.enrollmentservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.se347.enrollmentservice.services.LearningProgressService;
import com.se347.enrollmentservice.dtos.LearningProgressRequestDto;
import com.se347.enrollmentservice.dtos.LearningProgressResponseDto;

import java.util.UUID;
import java.util.List;

@RequestMapping("/api/learning-progress")
@RestController
public class LearningProgressController {
    private final LearningProgressService learningProgressService;

    LearningProgressController(LearningProgressService learningProgressService){
        this.learningProgressService = learningProgressService;
    }

    @PostMapping
    public ResponseEntity<LearningProgressResponseDto> createLearningProgress(
        @RequestBody LearningProgressRequestDto request,
        @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(learningProgressService.createLearningProgress(request, userId));
    }

    @GetMapping("/id/{learningProgressId}")
    public ResponseEntity<LearningProgressResponseDto> getLearningProgressById(
            @PathVariable UUID learningProgressId, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(learningProgressService.getLearningProgressById(learningProgressId, userId));
    }

    @GetMapping("/enrollment/id/{enrollmentId}")
    public ResponseEntity<List<LearningProgressResponseDto>> getLearningProgressByEnrollmentId(
            @PathVariable UUID enrollmentId, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(learningProgressService.getLearningProgressByEnrollmentId(enrollmentId, userId));
    } 

    @GetMapping("/lesson/id/{lessonId}/enrollment/id/{enrollmentId}")
    public ResponseEntity<LearningProgressResponseDto> getLearningProgressByLessonIdAndEnrollmentId(
        @PathVariable UUID lessonId, 
        @PathVariable UUID enrollmentId,
        @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(learningProgressService.getLearningProgressByLessonIdAndEnrollmentId(lessonId, enrollmentId, userId));
    }

    @PutMapping("/id/{learningProgressId}") 
    public ResponseEntity<LearningProgressResponseDto> updateLearningProgress(
        @PathVariable UUID learningProgressId,
        @RequestBody LearningProgressRequestDto request,
        @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(learningProgressService.updateLearningProgress(learningProgressId, request, userId));
    }
    
    @PostMapping("/lesson/id/{lessonId}/enrollment/id/{enrollmentId}/complete")
    public ResponseEntity<LearningProgressResponseDto> markAsCompleted(@PathVariable UUID lessonId, @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(learningProgressService.markAsCompleted(lessonId, enrollmentId));
    }
}
