package com.se347.enrollmentservice.services;

import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;

import java.util.UUID;

public interface CourseProgressService {
    CourseProgressResponseDto createCourseProgress(CourseProgressRequestDto request);
    CourseProgressResponseDto getCourseProgressById(UUID courseProgressId);
    CourseProgressResponseDto getCourseProgressByEnrollmentId(UUID enrollmentId);
    CourseProgressResponseDto updateCourseProgress(UUID courseProgressId, CourseProgressRequestDto request);
    CourseProgressResponseDto patchCourseProgress(UUID courseProgressId, CourseProgressRequestDto request);
    CourseProgressResponseDto updateLessonsCompleted(UUID courseProgressId, Integer lessonsCompleted);
    void setTotalLessons(UUID courseProgressId, Integer totalLessons);
}
