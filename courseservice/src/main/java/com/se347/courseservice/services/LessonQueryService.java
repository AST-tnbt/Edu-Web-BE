package com.se347.courseservice.services;

import com.se347.courseservice.dtos.LessonResponseDto;

import java.util.List;
import java.util.UUID;

public interface LessonQueryService {
    LessonResponseDto getLessonById(UUID lessonId);
    LessonResponseDto getLessonByLessonSlug(String lessonSlug);
    List<LessonResponseDto> getLessonsBySectionId(UUID sectionId);
    List<LessonResponseDto> getLessonsBySectionSlug(String sectionSlug);
}
