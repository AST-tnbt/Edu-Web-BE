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

    @PostMapping
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

    @GetMapping("/course/{courseId}/lessons")
    public ResponseEntity<List<LessonResponseDto>> getLessonsByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourseId(courseId));
    }

    // @DeleteMapping("/{lessonId}")
    // public ResponseEntity<Void> deleteLesson(@PathVariable String lessonId) {
    //     lessonService.deleteLesson(lessonId);
    //     return ResponseEntity.noContent().build();
    // }

    @PutMapping("/lessons/{lessonId}/reorder")
    public ResponseEntity<List<LessonResponseDto>> reorderLessons(@PathVariable UUID lessonId, @RequestBody List<LessonRequestDto> request) {
        return ResponseEntity.ok(lessonService.getLessonsByCourseId(lessonId));
    }
}
