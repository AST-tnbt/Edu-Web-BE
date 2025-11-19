package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;

import com.se347.courseservice.services.LessonService;
import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.exceptions.LessonException;

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
    public ResponseEntity<LessonResponseDto> getLessonById(@PathVariable UUID lessonId, @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new LessonException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        return ResponseEntity.ok(lessonService.getLessonById(lessonId));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponseDto> updateLesson(@PathVariable UUID lessonId, @RequestBody LessonRequestDto request, @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new LessonException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request));
    }

    @GetMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<List<LessonResponseDto>> getLessonsBySectionId(@PathVariable UUID sectionId, @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new LessonException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        return ResponseEntity.ok(lessonService.getLessonsBySectionId(sectionId));
    }

    @GetMapping("/courses/{courseSlug}/sections/{sectionSlug}/lessons")
    public ResponseEntity<List<LessonResponseDto>> getLessonsByCourseSlugAndSectionSlug(@PathVariable String courseSlug, @PathVariable String sectionSlug, @RequestHeader("X-User-Roles") String userRoles) {
        return ResponseEntity.ok(lessonService.getLessonsByCourseSlugAndSectionSlug(courseSlug, sectionSlug));
    }

    @GetMapping("/courses/{courseSlug}/sections/{sectionSlug}/lessons/{lessonSlug}")
    public ResponseEntity<LessonResponseDto> getLessonByCourseSlugAndSectionSlugAndLessonSlug(@PathVariable String courseSlug, @PathVariable String sectionSlug, @PathVariable String lessonSlug, @RequestHeader("X-User-Roles") String userRoles) {
        return ResponseEntity.ok(lessonService.getLessonByCourseSlugAndSectionSlugAndLessonSlug(courseSlug, sectionSlug, lessonSlug));
    }

    @PutMapping("/courses/{courseSlug}/sections/{sectionSlug}/lessons/{lessonSlug}")
    public ResponseEntity<LessonResponseDto> updateLessonByCourseSlugAndSectionSlugAndLessonSlug(@PathVariable String courseSlug, @PathVariable String sectionSlug, @PathVariable String lessonSlug, @RequestBody LessonRequestDto request, @RequestHeader("X-User-Roles") String userRoles, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(lessonService.updateLessonByCourseSlugAndSectionSlugAndLessonSlug(courseSlug, sectionSlug, lessonSlug, request, userRoles, UUID.fromString(userId)));
    }
}
