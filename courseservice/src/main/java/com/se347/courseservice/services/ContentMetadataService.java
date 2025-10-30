package com.se347.courseservice.services;

import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.dtos.ContentMetadataResponseDto;
import java.util.List;

import java.util.UUID;

public interface ContentMetadataService {
    ContentMetadataResponseDto createContentMetadata(ContentMetadataRequestDto request);
    ContentMetadataResponseDto getContentMetadataById(UUID contentId);
    ContentMetadataResponseDto updateContentMetadata(UUID contentId, ContentMetadataRequestDto request);
    List<ContentMetadataResponseDto> getAllContentMetadata();
    List<ContentMetadataResponseDto> getContentMetadataByCourseId(UUID courseId);
    List<ContentMetadataResponseDto> getContentMetadataByLessonId(UUID lessonId);
}
