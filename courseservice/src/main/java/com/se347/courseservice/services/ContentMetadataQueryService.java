package com.se347.courseservice.services;

import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import java.util.List;
import java.util.UUID;

/**
 * Query Service for Content READ operations
 * 
 * DDD + CQRS PRINCIPLES:
 * - ONLY read operations (queries)
 * - Direct repository access for performance
 * - No write operations (those go to CourseCommandService)
 * 
 * WRITE OPERATIONS:
 * - createContent() → CourseCommandService
 * - updateContent() → CourseCommandService
 * - publishContent() → CourseCommandService
 * - unpublishContent() → CourseCommandService
 */
public interface ContentMetadataQueryService {
    
    /**
     * Get all contents for a lesson
     * 
     * @param lessonId The lesson ID
     * @return List of contents
     */
    List<ContentMetadataResponseDto> getContentsByLessonId(UUID lessonId);
    
    /**
     * Get specific content by ID
     * 
     * @param lessonId The lesson ID (for validation)
     * @param contentId The content ID
     * @return Content details
     */
    ContentMetadataResponseDto getContent(UUID lessonId, UUID contentId);
}
