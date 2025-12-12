package com.se347.courseservice.domains.impls;

import com.se347.courseservice.domains.LessonDomainService;
import com.se347.courseservice.domains.SectionDomainService;
import com.se347.courseservice.repositories.LessonRepository;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.utils.SlugUtil;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonDomainServiceImpl implements LessonDomainService {
    
    private final LessonRepository lessonRepository;
    private final SectionDomainService sectionDomainService;

    // Lesson CRUD operations
    @Override
    @Transactional(readOnly = true)
    public Lesson findLessonById(UUID lessonId) {
        if (lessonId == null) {
            throw new CourseException.InvalidRequestException("Lesson ID cannot be null");
        }
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new CourseException.LessonNotFoundException(lessonId.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson findLessonBySlug(String lessonSlug) {
        if (lessonSlug == null || lessonSlug.isEmpty()) {
            throw new CourseException.InvalidRequestException("Lesson slug cannot be null or empty");
        }
        return lessonRepository.findByLessonSlug(lessonSlug)
                .orElseThrow(() -> new CourseException.LessonNotFoundException("Lesson with slug '" + lessonSlug + "' not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson toLesson(UUID lessonId) {
        return findLessonById(lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean lessonExists(UUID lessonId) {
        if (lessonId == null) {
            return false;
        }
        return lessonRepository.existsById(lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean lessonExistsBySlug(String lessonSlug) {
        if (lessonSlug == null || lessonSlug.isEmpty()) {
            return false;
        }
        return lessonRepository.findByLessonSlug(lessonSlug).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> findLessonsBySectionId(UUID sectionId) {
        if (sectionId == null) {
            throw new CourseException.InvalidRequestException("Section ID cannot be null");
        }
        return lessonRepository.findBySection_SectionId(sectionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> findLessonsByCourseSlugAndSectionSlug(String courseSlug, String sectionSlug) {
        if (courseSlug == null || courseSlug.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Course slug cannot be null or empty");
        }
        if (sectionSlug == null || sectionSlug.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Section slug cannot be null or empty");
        }
        
        Section section = sectionDomainService.findSectionBySlug(sectionSlug);
        // Validate section belongs to course
        if (!section.getCourse().getCourseSlug().equals(courseSlug)) {
            throw new CourseException.SectionNotFoundException("Section with slug '" + sectionSlug + "' not found in course '" + courseSlug + "'");
        }
        
        return lessonRepository.findBySection_SectionId(section.getSectionId());
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson findLessonByLessonSlug(String lessonSlug) {
        if (lessonSlug == null || lessonSlug.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Lesson slug cannot be null or empty");
        }
        
        return lessonRepository.findByLessonSlug(lessonSlug)
                .orElseThrow(() -> new CourseException.LessonNotFoundException("Lesson with slug '" + lessonSlug + "' not found"));
    }

    // Entity operations
    @Override
    public Lesson createLessonEntity(LessonRequestDto request, Section section) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (section == null) {
            throw new CourseException.InvalidRequestException("Section cannot be null");
        }
        
        String normalizedTitle = request.getTitle() != null ? request.getTitle().trim() : null;
        String lessonSlug = request.getLessonSlug() != null ? request.getLessonSlug() : generateLessonSlug(normalizedTitle);
        
        Lesson lesson = Lesson.builder()
            .lessonSlug(lessonSlug)
            .title(normalizedTitle)
            .section(section)
            .orderIndex(request.getOrderIndex())
            .build();
        
        lesson.onCreate();
        return lesson;
    }

    @Override
    public Lesson updateLessonEntity(Lesson lesson, LessonRequestDto request, Section section) {
        if (lesson == null) {
            throw new CourseException.LessonNotFoundException("Lesson cannot be null");
        }
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (section == null) {
            throw new CourseException.InvalidRequestException("Section cannot be null");
        }
        
        String normalizedTitle = request.getTitle() != null ? request.getTitle().trim() : null;
        String lessonSlug = request.getLessonSlug() != null ? request.getLessonSlug() : generateLessonSlug(normalizedTitle);
        
        lesson.setLessonSlug(lessonSlug);
        lesson.setTitle(normalizedTitle);
        lesson.setSection(section);
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setSection(section);
        lesson.onUpdate();
        
        return lesson;
    }

    // Business validations
    @Override
    public void validateLessonCreation(LessonRequestDto request) {
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        if (request.getSectionId() == null) {
            throw new CourseException.InvalidRequestException("Section ID cannot be null");
        }
        if (request.getOrderIndex() <= 0) {
            throw new CourseException.InvalidRequestException("Order index must be greater than 0");
        }
        
        // Validate section exists
        if (!sectionDomainService.sectionExists(request.getSectionId())) {
            throw new CourseException.SectionNotFoundException(request.getSectionId().toString());
        }
    }

    @Override
    public void validateLessonUpdate(Lesson lesson, LessonRequestDto request, UUID userId) {
        if (lesson == null) {
            throw new CourseException.LessonNotFoundException("Lesson cannot be null");
        }
        if (request == null) {
            throw new CourseException.InvalidRequestException("Request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        if (request.getSectionId() == null) {
            throw new CourseException.InvalidRequestException("Section ID cannot be null");
        }
        if (request.getOrderIndex() <= 0) {
            throw new CourseException.InvalidRequestException("Order index must be greater than 0");
        }
        if (!isLessonOwner(lesson, userId)) {
            throw new CourseException.UnauthorizedAccessException("User not authorized to access this resource");
        }
        // Validate section exists
        if (!sectionDomainService.sectionExists(request.getSectionId())) {
            throw new CourseException.SectionNotFoundException(request.getSectionId().toString());
        }
    }

    @Override
    public void validateLessonBelongsToSection(Lesson lesson, Section section) {
        if (lesson == null) {
            throw new CourseException.LessonNotFoundException("Lesson cannot be null");
        }
        if (section == null) {
            throw new CourseException.SectionNotFoundException("Section cannot be null");
        }
        if (!lesson.getSection().getSectionId().equals(section.getSectionId())) {
            throw new CourseException.LessonNotFoundException("Lesson does not belong to section");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLessonOwner(Lesson lesson, UUID userId) {
        if (lesson == null || userId == null) {
            return false;
        }
        return lesson.getSection().getCourse().getInstructorId().equals(userId);
    }

    // Slug management
    @Override
    public String generateLessonSlug(String title) {
        if (title == null || title.isEmpty()) {
            throw new CourseException.InvalidRequestException("Title cannot be null or empty");
        }
        return SlugUtil.toSlug(title);
    }
}

