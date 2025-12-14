package com.se347.courseservice.services.impl;

import org.springframework.stereotype.Service;

import com.se347.courseservice.services.ContentMetadataService;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import com.se347.courseservice.repositories.ContentRepository;
import com.se347.courseservice.entities.Content;
import com.se347.courseservice.domains.ContentDomainService;
import com.se347.courseservice.domains.LessonDomainService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ContentMetadataServiceImpl implements ContentMetadataService {

    private final ContentRepository contentRepository;
    private final ContentDomainService contentDomainService;
    private final LessonDomainService lessonDomainService;

    @Override
    @Transactional
    public ContentMetadataResponseDto createContentMetadata(ContentMetadataRequestDto request, UUID userId) {
        // Validate business rules through domain service
        contentDomainService.validateContentCreation(request, userId);
        
        // Create entity through domain service
        Content content = contentDomainService.createContentEntity(request, lessonDomainService.findLessonById(request.getLessonId()));
        
        // Save through repository (infrastructure concern)
        contentRepository.save(content);
        return mapToResponse(content);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentMetadataResponseDto getContentMetadataById(UUID contentId) {
        Content content = contentDomainService.findContentById(contentId);
        return mapToResponse(content);
    }

    @Override
    @Transactional
    public ContentMetadataResponseDto updateContentMetadata(UUID contentId, ContentMetadataRequestDto request, UUID userId) {
        // Get content through domain service
        Content content = contentDomainService.findContentById(contentId);
        
        // Validate business rules through domain service
        contentDomainService.validateContentUpdate(content, request, userId);
        
        // Update entity through domain service
        contentDomainService.updateContentEntity(content, request, lessonDomainService.findLessonById(request.getLessonId()));
        
        // Save through repository (infrastructure concern)
        contentRepository.save(content);
        return mapToResponse(content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentMetadataResponseDto> getContentMetadataByLessonId(UUID lessonId) {
        List<Content> contents = contentDomainService.findContentsByLessonId(lessonId);
        return contents.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private ContentMetadataResponseDto mapToResponse(Content content) {
        return ContentMetadataResponseDto.builder()
            .contentId(content.getContentId())
            .lessonId(content.getLesson().getLessonId())
            .contentType(content.getType())
            .title(content.getTitle())
            .contentUrl(content.getContentUrl())
            .textContent(content.getTextContent())
            .orderIndex(content.getOrderIndex())
            .status(content.getStatus())
            .createdAt(content.getCreatedAt())
            .updatedAt(content.getUpdatedAt())
            .build();
    }
}
