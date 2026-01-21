package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.enrollmentservice.entities.valueobjects.Percentage;
import com.se347.enrollmentservice.services.CourseProgressQueryService;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;
import com.se347.enrollmentservice.repositories.CourseProgressRepository;
import com.se347.enrollmentservice.entities.CourseProgress;
import com.se347.enrollmentservice.exceptions.CourseProgressException;

import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CourseProgressQueryServiceImpl implements CourseProgressQueryService {

    private final CourseProgressRepository courseProgressRepository;

    // ========== Public API ==========

    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponseDto getCourseProgressById(UUID courseProgressId) {
        CourseProgress courseProgress = courseProgressRepository.findByCourseProgressId(courseProgressId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException("Course progress not found with ID: " + courseProgressId));

        return mapToResponse(courseProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponseDto getCourseProgressByEnrollmentId(UUID enrollmentId) {
        CourseProgress courseProgress = courseProgressRepository.findByEnrollmentId(enrollmentId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException("Course progress not found with enrollment ID: " + enrollmentId));

        return mapToResponse(courseProgress);
    }

    // ========== Mapping ==========

    private CourseProgressResponseDto mapToResponse(CourseProgress courseProgress) {
        return CourseProgressResponseDto.builder()
            .courseProgressId(courseProgress.getCourseProgressId())
            .overallProgress(Percentage.of(courseProgress.getProgressPercentage()))
            .lessonsCompleted(courseProgress.getLessonsCompleted())
            .totalLessons(courseProgress.getTotalLessons())
            .isAllLessonsCompleted(courseProgress.isAllLessonsCompleted())
            .allLessonsCompletedAt(courseProgress.getAllLessonsCompletedAt())
            .createdAt(courseProgress.getCreatedAt())
            .updatedAt(courseProgress.getUpdatedAt())
            .build();
    }

}