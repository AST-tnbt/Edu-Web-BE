package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;

import com.se347.enrollmentservice.services.CourseProgressService;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;
import com.se347.enrollmentservice.repositories.CourseProgressRepository;
import com.se347.enrollmentservice.entities.CourseProgress;
import com.se347.enrollmentservice.exceptions.CourseProgressException;

import java.util.UUID;
import java.util.Optional;

@Service
public class CourseProgressServiceImpl implements CourseProgressService {

    private final CourseProgressRepository courseProgressRepository;

    public CourseProgressServiceImpl(CourseProgressRepository courseProgressRepository) {
        this.courseProgressRepository = courseProgressRepository;
    }

    @Override
    public CourseProgressResponseDto createCourseProgress(CourseProgressRequestDto request) {
    
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }

        CourseProgress courseProgress = CourseProgress.builder()
            .enrollmentId(request.getEnrollmentId())
            .overallProgress(request.getOverallProgress())
            .lessonsCompleted(request.getLessonsCompleted())
            .totalLessons(request.getTotalLessons())
            .isCourseCompleted(request.isCourseCompleted())
            .courseCompletedAt(request.getCourseCompletedAt())
            .build();

        courseProgress.onCreate();
        courseProgress.onUpdate();
        courseProgressRepository.save(courseProgress);

        return mapToResponse(courseProgress);
    }

    @Override
    public CourseProgressResponseDto getCourseProgressById(UUID courseProgressId) {
        CourseProgress courseProgress = courseProgressRepository.findById(courseProgressId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException(courseProgressId.toString()));
        return mapToResponse(courseProgress);
    }

    @Override
    public CourseProgressResponseDto getCourseProgressByEnrollmentId(UUID enrollmentId) {
        CourseProgress courseProgress = courseProgressRepository.findByEnrollmentId(enrollmentId);
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException(enrollmentId.toString());
        }
        return mapToResponse(courseProgress);
    }

    @Override
    public CourseProgressResponseDto updateCourseProgress(UUID courseProgressId, CourseProgressRequestDto request) {

        CourseProgress courseProgress = courseProgressRepository.findById(courseProgressId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException(courseProgressId.toString()));

        courseProgress.setOverallProgress(request.getOverallProgress());
        courseProgress.setLessonsCompleted(request.getLessonsCompleted());
        courseProgress.setTotalLessons(request.getTotalLessons());
        courseProgress.setCourseCompleted(request.isCourseCompleted());
        courseProgress.setCourseCompletedAt(request.getCourseCompletedAt());
        courseProgress.onUpdate();
        return mapToResponse(courseProgressRepository.save(courseProgress));
    }

    private CourseProgressResponseDto mapToResponse(CourseProgress courseProgress) {
        return CourseProgressResponseDto.builder()
            .courseProgressId(courseProgress.getCourseProgressId())
            .enrollmentId(courseProgress.getEnrollmentId())
            .overallProgress(courseProgress.getOverallProgress())
            .lessonsCompleted(courseProgress.getLessonsCompleted())
            .totalLessons(courseProgress.getTotalLessons())
            .isCourseCompleted(courseProgress.isCourseCompleted())
            .courseCompletedAt(courseProgress.getCourseCompletedAt())
            .createdAt(courseProgress.getCreatedAt())
            .updatedAt(courseProgress.getUpdatedAt())
            .build();
    }
}
