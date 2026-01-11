package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.se347.courseservice.services.SectionQueryService;
import com.se347.courseservice.services.CourseCommandService;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SectionController {

    private final SectionQueryService sectionService;
    private final CourseCommandService courseCommandService;

    @PostMapping("/courses/id/{courseId}/sections")
    public ResponseEntity<SectionResponseDto> createSection(
        @PathVariable UUID courseId, 
        @RequestBody SectionRequestDto request,
        @RequestHeader("X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(courseCommandService.createSection(courseId, request, userId));
    }

    @GetMapping("/courses/sections/id/{sectionId}")
    public ResponseEntity<SectionResponseDto> getSectionById(
            @PathVariable UUID sectionId) {
        return ResponseEntity.ok(sectionService.getSectionById(sectionId));
    }

    @PutMapping("/courses/id/{courseId}/sections/id/{sectionId}")
    public ResponseEntity<SectionResponseDto> updateSectionById(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId, 
            @RequestBody SectionRequestDto request, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(courseCommandService.updateSectionById(courseId, sectionId, request, userId));
    }

    @GetMapping("/courses/id/{courseId}/sections")
    public ResponseEntity<List<SectionResponseDto>> getSectionsByCourseId(
            @PathVariable UUID courseId, 
            @RequestHeader("X-User-Roles") String userRoles) {
        return ResponseEntity.ok(sectionService.getSectionsByCourseId(courseId));
    }

    @GetMapping("/courses/slug/{courseSlug}/sections")
    public ResponseEntity<List<SectionResponseDto>> getSectionsByCourseSlug(
            @PathVariable String courseSlug, 
            @RequestHeader("X-User-Roles") String userRoles) {
        return ResponseEntity.ok(sectionService.getSectionsByCourseSlug(courseSlug));
    }
            
    @GetMapping("/courses/sections/slug/{sectionSlug}")
    public ResponseEntity<SectionResponseDto> getSectionBySectionSlug(
            @PathVariable String sectionSlug) {
        return ResponseEntity.ok(sectionService.getSectionBySectionSlug(sectionSlug));
    }

    @PutMapping("/courses/slug/{courseSlug}/sections/slug/{sectionSlug}")
    public ResponseEntity<SectionResponseDto> updateSectionBySectionSlug(
            @PathVariable String courseSlug,
            @PathVariable String sectionSlug, 
            @RequestBody SectionRequestDto request, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(courseCommandService.updateSectionBySectionSlug(courseSlug, sectionSlug, request, userId));
    }
}
