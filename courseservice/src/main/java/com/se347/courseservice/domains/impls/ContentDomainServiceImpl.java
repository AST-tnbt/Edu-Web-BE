package com.se347.courseservice.domains.impls;

import com.se347.courseservice.domains.ContentDomainService;
import com.se347.courseservice.domains.LessonDomainService;
import com.se347.courseservice.repositories.ContentRepository;
import com.se347.courseservice.entities.Content;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.dtos.ContentMetadataRequestDto;
import com.se347.courseservice.exceptions.CourseException;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentDomainServiceImpl implements ContentDomainService {
    
    private final ContentRepository contentRepository;
    private final LessonDomainService lessonDomainService;

    // Content CRUD operations
    @Override
    @Transactional(readOnly = true)
    public Content findContentById(UUID contentId) {
        if (contentId == null) {
            throw new CourseException.InvalidRequestException("Content ID cannot be null");
        }
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new CourseException.ContentNotFoundException(contentId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Content> findContentsByLessonId(UUID lessonId) {
        if (lessonId == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }
        return contentRepository.findByLesson_LessonId(lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean contentExists(UUID contentId) {
        if (contentId == null) {
            return false;
        }
        return contentRepository.existsById(contentId);
    }

    // Entity operations
    @Override
    public Content createContentEntity(ContentMetadataRequestDto request, Lesson lesson) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (lesson == null) {
            throw new CourseException.InvalidRequestException("Lesson cannot be null");
        }
        
        String normalizedTitle = request.getTitle() != null ? request.getTitle().trim() : null;
        String normalizedUrl = request.getContentUrl() != null ? request.getContentUrl().trim() : null;
        String normalizedText = request.getTextContent() != null ? request.getTextContent().trim() : null;
        
        Content content = Content.builder()
            .lesson(lesson)
            .type(request.getContentType())
            .title(normalizedTitle)
            .contentUrl(normalizedUrl)
            .textContent(normalizedText)
            .orderIndex(request.getOrderIndex())
            .status(request.getStatus())
            .build();
        
        content.onCreate();
        return content;
    }

    @Override
    public Content updateContentEntity(Content content, ContentMetadataRequestDto request, Lesson lesson) {
        if (content == null) {
            throw new CourseException.ContentNotFoundException("Content cannot be null");
        }
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (lesson == null) {
            throw new CourseException.InvalidRequestException("Lesson cannot be null");
        }
        
        String normalizedTitle = request.getTitle() != null ? request.getTitle().trim() : null;
        String normalizedUrl = request.getContentUrl() != null ? request.getContentUrl().trim() : null;
        String normalizedText = request.getTextContent() != null ? request.getTextContent().trim() : null;
        
        content.setLesson(lesson);
        content.setType(request.getContentType());
        content.setTitle(normalizedTitle);
        content.setContentUrl(normalizedUrl);
        content.setTextContent(normalizedText);
        content.setOrderIndex(request.getOrderIndex());
        content.setStatus(request.getStatus());
        content.onUpdate();
        
        return content;
    }

    // Business validations
    @Override
    public void validateContentCreation(ContentMetadataRequestDto request, UUID userId) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (request.getLessonId() == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }
        if (request.getContentType() == null) {
            throw new CourseException.InvalidRequestException("Content type cannot be null");
        }
        
        if (!lessonDomainService.isLessonOwner(lessonDomainService.findLessonById(request.getLessonId()), userId)) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }

        // Validate lesson exists
        if (!lessonDomainService.lessonExists(request.getLessonId())) {
            throw new CourseException.LessonNotFoundException(request.getLessonId().toString());
        }
    }

    @Override
    public void validateContentUpdate(Content content, ContentMetadataRequestDto request, UUID userId) {
        if (content == null) {
            throw new CourseException.ContentNotFoundException("Content cannot be null");
        }
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (request.getLessonId() == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }
        if (request.getContentType() == null) {
            throw new CourseException.InvalidRequestException("Content type cannot be null");
        }
        
        if (!isContentOwner(content, userId)) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }

        // Validate lesson exists
        if (!lessonDomainService.lessonExists(request.getLessonId())) {
            throw new CourseException.LessonNotFoundException(request.getLessonId().toString());
        }
    }

    @Override
    public void validateContentBelongsToLesson(Content content, Lesson lesson) {
        if (content == null) {
            throw new CourseException.ContentNotFoundException("Content cannot be null");
        }
        if (lesson == null) {
            throw new CourseException.LessonNotFoundException("Lesson cannot be null");
        }
        if (!content.getLesson().getLessonId().equals(lesson.getLessonId())) {
            throw new CourseException.ContentNotFoundException("Content does not belong to lesson");
        }
    }

    @Override
    public boolean isContentOwner(Content content, UUID userId) {
        if (content == null || userId == null) {
            return false;
        }
        return content.getLesson().getSection().getCourse().getInstructorId().equals(userId);
    }
}

