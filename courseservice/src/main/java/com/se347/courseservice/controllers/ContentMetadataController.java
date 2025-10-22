package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;

import com.se347.courseservice.services.ContentMetadataService;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/contents")
@RestController
public class ContentMetadataController {
    
    private final ContentMetadataService contentMetadataService;

    ContentMetadataController(ContentMetadataService contentMetadataService){
        this.contentMetadataService = contentMetadataService;
    }

    @PostMapping
    public ResponseEntity<ContentMetadataResponseDto> createContentMetadata(@RequestBody ContentMetadataRequestDto request) {
        return ResponseEntity.ok(contentMetadataService.createContentMetadata(request));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentMetadataResponseDto> getContentMetadataById(@PathVariable UUID contentId) {
        return ResponseEntity.ok(contentMetadataService.getContentMetadataById(contentId));
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<ContentMetadataResponseDto> updateContentMetadata(@PathVariable UUID contentId, @RequestBody ContentMetadataRequestDto request) {
        return ResponseEntity.ok(contentMetadataService.updateContentMetadata(contentId, request));
    }

    @GetMapping
    public ResponseEntity<List<ContentMetadataResponseDto>> getAllContentMetadata() {
        return ResponseEntity.ok(contentMetadataService.getAllContentMetadata());
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ContentMetadataResponseDto>> getContentMetadataByCourseId(@PathVariable UUID courseId) {
        return ResponseEntity.ok(contentMetadataService.getContentMetadataByCourseId(courseId));
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<ContentMetadataResponseDto>> getContentMetadataByLessonId(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(contentMetadataService.getContentMetadataByLessonId(lessonId));
    }
}
