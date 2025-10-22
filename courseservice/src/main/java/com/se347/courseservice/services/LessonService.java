package com.se347.courseservice.services;

import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import java.util.List;

import java.util.UUID;

public interface LessonService {
    LessonResponseDto createLesson(LessonRequestDto request);
    LessonResponseDto getLessonById(UUID lessonId);
    LessonResponseDto updateLesson(UUID lessonId, LessonRequestDto request);
    List<LessonResponseDto> getLessonsByCourseId(UUID courseId);
}
