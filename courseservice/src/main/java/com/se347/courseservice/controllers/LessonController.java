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

    @PostMapping("courses/id/{courseId}/sections/id/{sectionId}/lessons")
    public ResponseEntity<LessonResponseDto> createLesson(
            @PathVariable UUID courseId, 
            @PathVariable UUID sectionId, 
            @RequestBody LessonRequestDto request) {
        return ResponseEntity.ok(lessonService.createLesson(courseId, sectionId, request));
    }

    @GetMapping("/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}")
    public ResponseEntity<LessonResponseDto> getLessonById(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonById(courseId, sectionId, lessonId));
    }

    @PutMapping("/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}")
    public ResponseEntity<LessonResponseDto> updateLessonById(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @PathVariable UUID lessonId, 
            @RequestBody LessonRequestDto request, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(lessonService.updateLessonById(courseId, sectionId, lessonId, request, userId));
    }

    @GetMapping("/courses/slug/{courseSlug}/sections/slug/{sectionSlug}/lessons/slug/{lessonSlug}")
    public ResponseEntity<LessonResponseDto> getLessonByLessonSlug(
            @PathVariable String courseSlug,
            @PathVariable String sectionSlug,
            @PathVariable String lessonSlug) {
        return ResponseEntity.ok(lessonService.getLessonByLessonSlug(courseSlug, sectionSlug, lessonSlug));
    }

    @PutMapping("/courses/slug/{courseSlug}/sections/slug/{sectionSlug}/lessons/slug/{lessonSlug}")
    public ResponseEntity<LessonResponseDto> updateLessonByLessonSlug(
            @PathVariable String courseSlug,
            @PathVariable String sectionSlug,
            @PathVariable String lessonSlug, 
            @RequestBody LessonRequestDto request, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(lessonService.updateLessonByLessonSlug(courseSlug, sectionSlug, lessonSlug, request, userId));
    }

    @GetMapping("/courses/id/{courseId}/sections/id/{sectionId}/lessons")
    public ResponseEntity<List<LessonResponseDto>> getLessonsBySectionId(
        @PathVariable UUID courseId, 
        @PathVariable UUID sectionId) {
        return ResponseEntity.ok(lessonService.getLessonsBySectionId(courseId, sectionId));
    }

    @GetMapping("/courses/slug/{courseSlug}/sections/slug/{sectionSlug}/lessons")
    public ResponseEntity<List<LessonResponseDto>> getLessonsBySectionSlug(
        @PathVariable String courseSlug,
        @PathVariable String sectionSlug) {
        return ResponseEntity.ok(lessonService.getLessonsBySectionSlug(courseSlug, sectionSlug));
    }
}
