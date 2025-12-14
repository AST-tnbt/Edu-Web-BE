package com.se347.courseservice.domains;

import java.util.UUID;
import java.util.List;
import com.se347.courseservice.entities.Lesson;
import com.se347.courseservice.entities.Section;
import com.se347.courseservice.dtos.LessonRequestDto;

public interface LessonDomainService {
    // Lesson CRUD operations (trả về entity)
    Lesson findLessonById(UUID lessonId);
    Lesson findLessonBySlug(String lessonSlug);
    Lesson toLesson(UUID lessonId);
    boolean lessonExists(UUID lessonId);
    boolean lessonExistsBySlug(String lessonSlug);
    List<Lesson> findLessonsBySectionId(UUID sectionId);
    List<Lesson> findLessonsByCourseSlugAndSectionSlug(String courseSlug, String sectionSlug);
    Lesson findLessonByLessonSlug(String lessonSlug);

    // Entity operations
    Lesson createLessonEntity(LessonRequestDto request, Section section);
    Lesson updateLessonEntity(Lesson lesson, LessonRequestDto request, Section section);

    // Business validations
    void validateLessonCreation(LessonRequestDto request);
    void validateLessonUpdate(Lesson lesson, LessonRequestDto request, UUID userId);
    void validateLessonBelongsToSection(Lesson lesson, Section section);
    boolean isLessonOwner(Lesson lesson, UUID userId);
    
    // Slug management
    String generateLessonSlug(String title);
}

