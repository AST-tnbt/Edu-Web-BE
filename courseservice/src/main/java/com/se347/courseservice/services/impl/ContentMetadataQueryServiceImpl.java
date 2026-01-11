package com.se347.courseservice.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.courseservice.services.ContentMetadataQueryService;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import com.se347.courseservice.repositories.ContentRepository;
import com.se347.courseservice.entities.Content;
import com.se347.courseservice.repositories.LessonRepository;
import com.se347.courseservice.exceptions.LessonException;
import com.se347.courseservice.exceptions.ContentMetadataException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Application Service for Content operations
 * 
 * DDD STRATEGY (Pragmatic Approach):
 * 
 * URL DESIGN:
 *   → /api/lessons/{lessonId}/contents (simple, practical)
 *   → lessonId is globally unique (UUID), no need for courseId/sectionId
 * 
 * INTERNAL IMPLEMENTATION:
 *   → WRITES: Through Lesson aggregate (respects DDD boundaries)
 *   → READS: Direct repository (performance optimization)
 *   → Authorization: Delegated to Course via lesson.ensureOwnedBy()
 * 
 * KEY PRINCIPLE:
 *   → "URL design ≠ Aggregate design" (Vaughn Vernon)
 *   → External API can be pragmatic for UX
 *   → Internal logic must respect aggregate boundaries
 */
@RequiredArgsConstructor
@Service
public class ContentMetadataQueryServiceImpl implements ContentMetadataQueryService {

    private final ContentRepository contentRepository;
    private final LessonRepository lessonRepository;

    /**
     * Get all contents for a lesson
     * 
     * Strategy: Direct repository access for performance
     * - No need to enforce invariants (read-only)
     * - No authorization needed (public read)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ContentMetadataResponseDto> getContentsByLessonId(UUID lessonId) {
        // Validate lesson exists
        if (!lessonRepository.existsById(lessonId)) {
            throw new LessonException.LessonNotFoundException(lessonId.toString());
        }
        
        // Load contents directly (performance optimization)
        List<Content> contents = contentRepository.findByLesson_LessonId(lessonId);
        
        return contents.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get specific content by ID
     * 
     * Strategy: Direct repository + validation
     * - Load content directly (performance)
     * - Validate it belongs to the lesson
     */
    @Override
    @Transactional(readOnly = true)
    public ContentMetadataResponseDto getContent(UUID lessonId, UUID contentId) {
        // Load content directly
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ContentMetadataException.ContentNotFoundException(contentId.toString()));
        
        // Validate belongs to lesson (entity's responsibility)
        if (!content.belongsToLesson(lessonId)) {
            throw new ContentMetadataException.ContentNotFoundException(
                "Content " + contentId + " does not belong to lesson " + lessonId
            );
        }
        
        return mapToResponse(content);
    }

    // ========== MAPPING ==========
    
    private ContentMetadataResponseDto mapToResponse(Content content) {
        return ContentMetadataResponseDto.builder()
            .contentId(content.getContentId())
            .lessonId(content.getLesson().getLessonId())
            .contentType(content.getType())
            .title(content.getTitle())
            .contentUrl(content.getContentUrl())
            .textContent(content.getTextContent())
            .orderIndex(content.getOrderIndex().getValue()) // Extract from Value Object
            .status(content.getStatus())
            .createdAt(content.getCreatedAt())
            .updatedAt(content.getUpdatedAt())
            .build();
    }
}
