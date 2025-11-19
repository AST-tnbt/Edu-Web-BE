package com.se347.courseservice.services;

import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.entities.Lesson;

import java.util.List;
import java.util.UUID;

public interface LessonService {
    LessonResponseDto createLesson(LessonRequestDto request);
    LessonResponseDto getLessonById(UUID lessonId);
    Lesson toLesson(UUID lessonId);
    LessonResponseDto updateLesson(UUID lessonId, LessonRequestDto request);
    List<LessonResponseDto> getLessonsBySectionId(UUID sectionId);
    List<LessonResponseDto> getLessonsByCourseSlugAndSectionSlug(String courseSlug, String sectionSlug);
    LessonResponseDto getLessonByCourseSlugAndSectionSlugAndLessonSlug(String courseSlug, String sectionSlug, String lessonSlug);
    LessonResponseDto updateLessonByCourseSlugAndSectionSlugAndLessonSlug(String courseSlug, String sectionSlug, String lessonSlug, LessonRequestDto request, String userRoles, UUID userId);
}
