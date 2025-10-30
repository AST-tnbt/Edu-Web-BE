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
    public ResponseEntity<LearningProgressResponseDto> createLearningProgress(@RequestBody LearningProgressRequestDto request) {
        return ResponseEntity.ok(learningProgressService.createLearningProgress(request));
    }

    @GetMapping("{learningProgressId}")
    public ResponseEntity<LearningProgressResponseDto> getLearningProgressById(@PathVariable UUID learningProgressId) {
        return ResponseEntity.ok(learningProgressService.getLearningProgressById(learningProgressId));
    }

    @GetMapping("enrollment/{enrollmentId}")
    public ResponseEntity<List<LearningProgressResponseDto>> getLearningProgressByEnrollmentId(@PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(learningProgressService.getLearningProgressByEnrollmentId(enrollmentId));
    } 

    @PutMapping("{learningProgressId}") 
    public ResponseEntity<LearningProgressResponseDto> updateLearningProgress(@PathVariable UUID learningProgressId, @RequestBody LearningProgressRequestDto request) {
        return ResponseEntity.ok(learningProgressService.updateLearningProgress(learningProgressId, request));
    }
    
    @PostMapping("{learningProgressId}/complete")
    public ResponseEntity<Void> markLearningProgressAsCompleted(@PathVariable UUID learningProgressId, @PathVariable UUID enrollmentId) {
        learningProgressService.markLearningProgressAsCompleted(learningProgressId, enrollmentId);
        return ResponseEntity.ok().build();
    }
}
