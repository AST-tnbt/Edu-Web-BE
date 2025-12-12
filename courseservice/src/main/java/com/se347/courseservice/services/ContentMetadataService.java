package com.se347.courseservice.services;

import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import java.util.List;

import java.util.UUID;

public interface ContentMetadataService {
    ContentMetadataResponseDto createContentMetadata(ContentMetadataRequestDto request, UUID userId);
    ContentMetadataResponseDto getContentMetadataById(UUID contentId);
    ContentMetadataResponseDto updateContentMetadata(UUID contentId, ContentMetadataRequestDto request, UUID userId);
    List<ContentMetadataResponseDto> getContentMetadataByLessonId(UUID lessonId);
}
