package com.se347.courseservice.services.impl;

import org.springframework.stereotype.Service;

import com.se347.courseservice.services.ContentMetadataService;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import com.se347.courseservice.repositories.ContentRepository;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.entities.Content;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContentMetadataServiceImpl implements ContentMetadataService {

    private final ContentRepository contentRepository;

    public ContentMetadataServiceImpl(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public ContentMetadataResponseDto createContentMetadata(ContentMetadataRequestDto request) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }

        if (request.getCourseId() == null) {
            throw new CourseException.InvalidRequestException("Course ID cannot be null");
        }

        if (request.getLessonId() == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }

        if (request.getContentId() == null) {
            throw new CourseException.InvalidRequestException("Content ID cannot be null");
        }

        if (request.getContentType() == null) {
            throw new CourseException.InvalidRequestException("Content type cannot be null");
        }

        Content content = Content.builder()
            .contentId(request.getContentId())
            .lessonId(request.getLessonId())
            .type(request.getContentType())
            .title(request.getTitle())
            .contentUrl(request.getContentUrl())
            .textContent(request.getTextContent())
            .orderIndex(request.getOrderIndex())
            .status(request.getStatus())
            .build();

        content.onCreate();
        contentRepository.save(content);
        return mapToResponse(content);
    }

    @Override
    public ContentMetadataResponseDto getContentMetadataById(UUID contentId) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new CourseException.ContentNotFoundException(contentId.toString()));
        return mapToResponse(content);
    }

    @Override
    public ContentMetadataResponseDto updateContentMetadata(UUID contentId, ContentMetadataRequestDto request) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new CourseException.ContentNotFoundException(contentId.toString()));

        if (request.getContentId() == null) {
            throw new CourseException.InvalidRequestException("Content ID cannot be null");
        }
        
        if (request.getLessonId() == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }

        content.setContentId(request.getContentId());
        content.setLessonId(request.getLessonId());
        content.setType(request.getContentType());
        content.setTitle(request.getTitle());
        content.setContentUrl(request.getContentUrl());
        content.setTextContent(request.getTextContent());
        content.setOrderIndex(request.getOrderIndex());
        content.setStatus(request.getStatus());
        content.onUpdate();
        contentRepository.save(content);
        return mapToResponse(content);
    }

    @Override
    public List<ContentMetadataResponseDto> getAllContentMetadata() {
        List<Content> contents = contentRepository.findAll();
        return contents.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<ContentMetadataResponseDto> getContentMetadataByCourseId(UUID courseId) {
        List<Content> contents = contentRepository.findByCourseId(courseId);
        return contents.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
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
            .lessonId(content.getLessonId())
            .contentType(content.getType())
            .title(content.getTitle())
            .contentUrl(content.getContentUrl())
            .textContent(content.getTextContent())
            .orderIndex(content.getOrderIndex())
            .status(content.getStatus())
            .build();
    }
}
