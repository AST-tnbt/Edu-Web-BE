package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.se347.courseservice.services.SectionService;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping("/courses/id/{courseId}/sections")
    public ResponseEntity<SectionResponseDto> createSection(@PathVariable UUID courseId, @RequestBody SectionRequestDto request) {
        return ResponseEntity.ok(sectionService.createSection(courseId, request));
    }

    @GetMapping("/courses/id/{courseId}/sections/id/{sectionId}")
    public ResponseEntity<SectionResponseDto> getSectionById(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId) {
        return ResponseEntity.ok(sectionService.getSectionById(courseId, sectionId));
    }

    @PutMapping("/courses/id/{courseId}/sections/id/{sectionId}")
    public ResponseEntity<SectionResponseDto> updateSectionById(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId, 
            @RequestBody SectionRequestDto request, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(sectionService.updateSectionById(courseId, sectionId, request, userId));
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
            
    @GetMapping("/courses/slug/{courseSlug}/sections/slug/{sectionSlug}")
    public ResponseEntity<SectionResponseDto> getSectionBySectionSlug(
            @PathVariable String courseSlug,
            @PathVariable String sectionSlug, 
            @RequestHeader("X-User-Roles") String userRoles) {
        return ResponseEntity.ok(sectionService.getSectionBySectionSlug(courseSlug, sectionSlug));
    }

    @PutMapping("/courses/slug/{courseSlug}/sections/slug/{sectionSlug}")
    public ResponseEntity<SectionResponseDto> updateSectionBySectionSlug(
            @PathVariable String courseSlug,
            @PathVariable String sectionSlug, 
            @RequestBody SectionRequestDto request, 
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(sectionService.updateSectionBySectionSlug(courseSlug, sectionSlug, request, userId));
    }
}
