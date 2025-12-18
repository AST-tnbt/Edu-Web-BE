package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;

import com.se347.courseservice.services.LessonQueryService;
import com.se347.courseservice.services.CourseCommandService;
import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LessonController {
    private final LessonQueryService lessonService;
    private final CourseCommandService courseCommandService;

    @PostMapping("courses/id/{courseId}/sections/id/{sectionId}/lessons")
    public ResponseEntity<LessonResponseDto> createLesson(
            @PathVariable UUID courseId, 
            @PathVariable UUID sectionId, 
            @RequestBody LessonRequestDto request,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(courseCommandService.createLesson(courseId, sectionId, request, userId));
    }

    @GetMapping("/courses/lessons/id/{lessonId}")
    public ResponseEntity<LessonResponseDto> getLessonById(
            @PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonById(lessonId));
    }

    @PutMapping("/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}")
    public ResponseEntity<LessonResponseDto> updateLessonById(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @PathVariable UUID lessonId, 
            @RequestBody LessonRequestDto request, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(courseCommandService.updateLessonById(courseId, sectionId, lessonId, request, userId));
    }

    @GetMapping("/courses/lessons/slug/{lessonSlug}")
    public ResponseEntity<LessonResponseDto> getLessonByLessonSlug(
            @PathVariable String lessonSlug) {
        return ResponseEntity.ok(lessonService.getLessonByLessonSlug(lessonSlug));
    }

    @PutMapping("/courses/slug/{courseSlug}/sections/slug/{sectionSlug}/lessons/slug/{lessonSlug}")
    public ResponseEntity<LessonResponseDto> updateLessonByLessonSlug(
            @PathVariable String courseSlug,
            @PathVariable String sectionSlug,
            @PathVariable String lessonSlug, 
            @RequestBody LessonRequestDto request, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(courseCommandService.updateLessonByLessonSlug(courseSlug, sectionSlug, lessonSlug, request, userId));
    }

    @GetMapping("/courses/sections/id/{sectionId}/lessons")
    public ResponseEntity<List<LessonResponseDto>> getLessonsBySectionId(
        @PathVariable UUID courseId, 
        @PathVariable UUID sectionId) {
        return ResponseEntity.ok(lessonService.getLessonsBySectionId(sectionId));
    }

    @GetMapping("/courses/sections/slug/{sectionSlug}/lessons")
    public ResponseEntity<List<LessonResponseDto>> getLessonsBySectionSlug(
        @PathVariable String sectionSlug) {
        return ResponseEntity.ok(lessonService.getLessonsBySectionSlug(sectionSlug));
    }
}
