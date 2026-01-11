package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.se347.courseservice.services.CourseCommandService;
import com.se347.courseservice.services.ContentMetadataQueryService;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Content operations
 * 
 * URL DESIGN (RESTful + DDD):
 * - Reflects aggregate hierarchy: Lesson → Content
 * - All Content operations scoped under Lesson
 * 
 * Endpoints:
 * POST   /api/lessons/{lessonId}/contents              - Create content
 * GET    /api/lessons/{lessonId}/contents              - List all contents
 * GET    /api/lessons/{lessonId}/contents/{contentId}  - Get specific content
 * PUT    /api/lessons/{lessonId}/contents/{contentId}  - Update content
 * DELETE /api/lessons/{lessonId}/contents/{contentId}  - Delete content (future)
 * POST   /api/lessons/{lessonId}/contents/{contentId}/publish - Publish content
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ContentMetadataController {
    
    private final ContentMetadataQueryService contentMetadataService;
    private final CourseCommandService courseCommandService;

    /**
     * Create new content in a lesson
     * 
     * POST /api/lessons/{lessonId}/contents
     */
    @PostMapping("/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}/contents")
    public ResponseEntity<ContentMetadataResponseDto> createContent(
        @PathVariable UUID courseId,
        @PathVariable UUID sectionId,
        @PathVariable UUID lessonId,
        @RequestBody ContentMetadataRequestDto request,
        @RequestHeader("X-User-Id") UUID userId) {
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(courseCommandService.createContent(courseId, sectionId, lessonId, request, userId));
    }

    /**
     * Get all contents for a lesson
     * 
     * GET /api/lessons/{lessonId}/contents
     */
    @GetMapping("/courses/lessons/id/{lessonId}/contents")
    public ResponseEntity<List<ContentMetadataResponseDto>> getContents(
        @PathVariable UUID lessonId) {
        
        return ResponseEntity.ok(contentMetadataService.getContentsByLessonId(lessonId));
    }

    /**
     * Get specific content by ID
     * 
     * GET /api/lessons/{lessonId}/contents/{contentId}
     */
    @GetMapping("/courses/content/id/{contentId}")
    public ResponseEntity<ContentMetadataResponseDto> getContent(
        @PathVariable UUID lessonId,
        @PathVariable UUID contentId) {
        
        return ResponseEntity.ok(contentMetadataService.getContent(lessonId, contentId));
    }

    /**
     * Update content
     * 
     * PUT /api/lessons/{lessonId}/contents/{contentId}
     */
    @PutMapping("/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}/contents/id/{contentId}")
    public ResponseEntity<ContentMetadataResponseDto> updateContent(
        @PathVariable UUID courseId,
        @PathVariable UUID sectionId,
        @PathVariable UUID lessonId,
        @PathVariable UUID contentId, 
        @RequestBody ContentMetadataRequestDto request,
        @RequestHeader("X-User-Id") UUID userId) {
        
        return ResponseEntity.ok(
            courseCommandService.updateContentById(courseId, sectionId, lessonId, contentId, request, userId)
        );
    }

    /**
     * Publish content (make it available to students)
     * 
     * POST /api/courses/content/id/{contentId}/publish
     * 
     * DDD + CQRS: Write operation → goes through CourseCommandService
     */
    @PostMapping("/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}/contents/id/{contentId}/publish")
    public ResponseEntity<ContentMetadataResponseDto> publishContent(
        @PathVariable UUID courseId,
        @PathVariable UUID sectionId,
        @PathVariable UUID lessonId,
        @PathVariable UUID contentId,
        @RequestHeader("X-User-Id") UUID userId) {
        
        return ResponseEntity.ok(
            courseCommandService.publishContent(courseId, sectionId, lessonId, contentId, userId)
        );
    }

    /**
     * Unpublish content (draft mode)
     * 
     * POST /api/courses/content/id/{contentId}/unpublish
     * 
     * DDD + CQRS: Write operation → goes through CourseCommandService
     */
    @PostMapping("/courses/id/{courseId}/sections/id/{sectionId}/lessons/id/{lessonId}/contents/id/{contentId}/unpublish")
    public ResponseEntity<ContentMetadataResponseDto> unpublishContent(
        @PathVariable UUID courseId,
        @PathVariable UUID sectionId,
        @PathVariable UUID lessonId,
        @PathVariable UUID contentId,
        @RequestHeader("X-User-Id") UUID userId) {
        
        return ResponseEntity.ok(
            courseCommandService.unpublishContent(courseId, sectionId, lessonId, contentId, userId)
        );
    }
}
