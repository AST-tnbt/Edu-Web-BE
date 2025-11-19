package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.se347.courseservice.services.SectionService;
import com.se347.courseservice.dtos.SectionRequestDto;
import com.se347.courseservice.dtos.SectionResponseDto;
import com.se347.courseservice.exceptions.SectionException;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping("/sections")
    public ResponseEntity<SectionResponseDto> createSection(@RequestBody SectionRequestDto request) {
        return ResponseEntity.ok(sectionService.createSection(request));
    }

    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<SectionResponseDto> getSectionById(@PathVariable UUID sectionId, @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new SectionException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        return ResponseEntity.ok(sectionService.getSectionById(sectionId));
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<SectionResponseDto> updateSection(@PathVariable UUID sectionId, @RequestBody SectionRequestDto request, @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new SectionException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        return ResponseEntity.ok(sectionService.updateSection(sectionId, request));
    }

    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<List<SectionResponseDto>> getSectionsByCourseId(@PathVariable UUID courseId, @RequestHeader("X-User-Roles") String userRoles) {
        if (!userRoles.contains("ADMIN")) {
            throw new SectionException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        return ResponseEntity.ok(sectionService.getSectionsByCourseId(courseId));
    }

    @GetMapping("/courses/{courseSlug}/sections")
    public ResponseEntity<List<SectionResponseDto>> getSectionsByCourseSlug(@PathVariable String courseSlug, @RequestHeader("X-User-Roles") String userRoles) {
        return ResponseEntity.ok(sectionService.getSectionsByCourseSlug(courseSlug));
    }
            
    @GetMapping("/courses/{courseSlug}/sections/{sectionSlug}")
    public ResponseEntity<SectionResponseDto> getSectionBySectionSlug(@PathVariable String courseSlug, @PathVariable String sectionSlug, @RequestHeader("X-User-Roles") String userRoles) {
        return ResponseEntity.ok(sectionService.getSectionByCourseSlugAndSectionSlug(courseSlug, sectionSlug));
    }

    @PutMapping("/courses/{courseSlug}/sections/{sectionSlug}")
    public ResponseEntity<SectionResponseDto> updateSectionByCourseSlugAndSectionSlug(@PathVariable String courseSlug, @PathVariable String sectionSlug, @RequestBody SectionRequestDto request, @RequestHeader("X-User-Roles") String userRoles, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(sectionService.updateSectionByCourseSlugAndSectionSlug(courseSlug, sectionSlug, request, userRoles, UUID.fromString(userId)));
    }
}
