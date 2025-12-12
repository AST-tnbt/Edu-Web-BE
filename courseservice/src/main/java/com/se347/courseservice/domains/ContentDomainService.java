package com.se347.courseservice.domains;

import java.util.UUID;
import java.util.List;
import com.se347.courseservice.entities.Content;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;

public interface ContentDomainService {
    // Content CRUD operations (trả về entity)
    Content findContentById(UUID contentId);
    List<Content> findContentsByLessonId(UUID lessonId);
    boolean contentExists(UUID contentId);

    // Entity operations
    Content createContentEntity(ContentMetadataRequestDto request, Lesson lesson);
    Content updateContentEntity(Content content, ContentMetadataRequestDto request, Lesson lesson);

    // Authorization
    boolean isContentOwner(Content content, UUID userId);
    // Business validations
    void validateContentCreation(ContentMetadataRequestDto request, UUID userId);
    void validateContentUpdate(Content content, ContentMetadataRequestDto request, UUID userId);
    void validateContentBelongsToLesson(Content content, Lesson lesson);
}

