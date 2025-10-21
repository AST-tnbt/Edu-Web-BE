package com.se347.courseservice.services;

import com.se347.courseservice.dtos.LessonRequestDto;
import com.se347.courseservice.dtos.LessonResponseDto;
import java.util.List;

public interface LessonService {
    LessonResponseDto createLesson(LessonRequestDto request);
    LessonResponseDto getLessonById(String lessonId);
    LessonResponseDto updateLesson(String lessonId, LessonRequestDto request);
    List<LessonResponseDto> getLessonsByCourseId(String courseId);
}
