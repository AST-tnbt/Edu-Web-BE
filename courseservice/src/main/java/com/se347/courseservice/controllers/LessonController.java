package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;

import com.se347.courseservice.services.LessonService;
import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class LessonController {
    private final LessonService lessonService;

    LessonController(LessonService lessonService){
        this.lessonService = lessonService;
    }

    @PostMapping("/lessons")
    public ResponseEntity<LessonResponseDto> createLesson(@RequestBody LessonRequestDto request) {
        return ResponseEntity.ok(lessonService.createLesson(request));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponseDto> getLessonById(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonById(lessonId));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponseDto> updateLesson(@PathVariable UUID lessonId, @RequestBody LessonRequestDto request) {
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request));
    }

    @GetMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<List<LessonResponseDto>> getLessonsBySectionId(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(lessonService.getLessonsBySectionId(sectionId));
    }

    // @DeleteMapping("/{lessonId}")
    // public ResponseEntity<Void> deleteLesson(@PathVariable String lessonId) {
    //     lessonService.deleteLesson(lessonId);
    //     return ResponseEntity.noContent().build();
    // }

    // Reorder endpoint can be added later if service supports it
}
