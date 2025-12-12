package com.se347.courseservice.services;

import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import com.se347.courseservice.entities.Lesson;

import java.util.List;
import java.util.UUID;

public interface LessonService {
    LessonResponseDto createLesson(UUID courseId, UUID sectionId, LessonRequestDto request);
    LessonResponseDto getLessonById(UUID courseId, UUID sectionId, UUID lessonId);
    LessonResponseDto getLessonByLessonSlug(String courseSlug, String sectionSlug, String lessonSlug);
    LessonResponseDto updateLessonById(UUID courseId, UUID sectionId, UUID lessonId, LessonRequestDto request, UUID userId);
    LessonResponseDto updateLessonByLessonSlug(String courseSlug, String sectionSlug, String lessonSlug, LessonRequestDto request, UUID userId);
    List<LessonResponseDto> getLessonsBySectionId(UUID courseId, UUID sectionId);
    List<LessonResponseDto> getLessonsBySectionSlug(String courseSlug, String sectionSlug);
    Lesson toLesson(UUID lessonId);
}
