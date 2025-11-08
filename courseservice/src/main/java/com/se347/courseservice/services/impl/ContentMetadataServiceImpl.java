package com.se347.courseservice.services.impl;

import org.springframework.stereotype.Service;

import com.se347.courseservice.services.ContentMetadataService;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import com.se347.courseservice.repositories.ContentRepository;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.entities.Content;
import com.se347.courseservice.services.LessonService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ContentMetadataServiceImpl implements ContentMetadataService {

    private final ContentRepository contentRepository;
    private final LessonService lessonService;

    @Override
    @Transactional
    public ContentMetadataResponseDto createContentMetadata(ContentMetadataRequestDto request) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }

        if (request.getLessonId() == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }

        if (request.getContentType() == null) {
            throw new CourseException.InvalidRequestException("Content type cannot be null");
        }

        String normalizedTitle = request.getTitle() != null ? request.getTitle().trim() : null;
        String normalizedUrl = request.getContentUrl() != null ? request.getContentUrl().trim() : null;
        String normalizedText = request.getTextContent() != null ? request.getTextContent().trim() : null;

        Content content = Content.builder()
            .lesson(lessonService.toLesson(request.getLessonId()))
            .type(request.getContentType())
            .title(normalizedTitle)
            .contentUrl(normalizedUrl)
            .textContent(normalizedText)
            .orderIndex(request.getOrderIndex())
            .status(request.getStatus())
            .build();

        content.onCreate();
        contentRepository.save(content);
        return mapToResponse(content);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentMetadataResponseDto getContentMetadataById(UUID contentId) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new CourseException.ContentNotFoundException(contentId.toString()));
        return mapToResponse(content);
    }

    @Override
    @Transactional
    public ContentMetadataResponseDto updateContentMetadata(UUID contentId, ContentMetadataRequestDto request) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new CourseException.ContentNotFoundException(contentId.toString()));

        if (request.getLessonId() == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }

        String normalizedTitle = request.getTitle() != null ? request.getTitle().trim() : null;
        String normalizedUrl = request.getContentUrl() != null ? request.getContentUrl().trim() : null;
        String normalizedText = request.getTextContent() != null ? request.getTextContent().trim() : null;

        content.setLesson(lessonService.toLesson(request.getLessonId()));
        content.setType(request.getContentType());
        content.setTitle(normalizedTitle);
        content.setContentUrl(normalizedUrl);
        content.setTextContent(normalizedText);
        content.setOrderIndex(request.getOrderIndex());
        content.setStatus(request.getStatus());
        content.onUpdate();
        contentRepository.save(content);
        return mapToResponse(content);
    }

    @Override
    public List<ContentMetadataResponseDto> getContentMetadataByLessonId(UUID lessonId) {
        List<Content> contents = contentRepository.findByLessonId(lessonId);
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
