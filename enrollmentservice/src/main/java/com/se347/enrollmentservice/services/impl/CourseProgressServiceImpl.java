package com.se347.enrollmentservice.services.impl;

import org.springframework.stereotype.Service;

import com.se347.enrollmentservice.services.CourseProgressService;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.dtos.CourseProgressResponseDto;
import com.se347.enrollmentservice.repositories.CourseProgressRepository;
import com.se347.enrollmentservice.entities.CourseProgress;
import com.se347.enrollmentservice.exceptions.CourseProgressException;
import com.se347.enrollmentservice.services.EnrollmentService;

import java.lang.Integer;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class CourseProgressServiceImpl implements CourseProgressService {

    private final CourseProgressRepository courseProgressRepository;
    private final EnrollmentService enrollmentService;

    public CourseProgressServiceImpl(CourseProgressRepository courseProgressRepository,
                                   EnrollmentService enrollmentService) {
        this.courseProgressRepository = courseProgressRepository;
        this.enrollmentService = enrollmentService;
    }

    // ========== Public API ==========

    @Override
    public CourseProgressResponseDto createCourseProgress(CourseProgressRequestDto request) {
        validateCreateRequest(request);
        
        // Verify enrollment exists
        if (!enrollmentService.isEnrollmentExists(request.getEnrollmentId())) {
            throw new CourseProgressException.InvalidRequestException(
                "Enrollment not found with ID: " + request.getEnrollmentId());
        }

        CourseProgress courseProgress = CourseProgress.builder()
            .enrollmentId(request.getEnrollmentId())
            .lessonsCompleted(request.getLessonsCompleted())
            .totalLessons(request.getTotalLessons())
            .isCourseCompleted(false)
            .courseCompletedAt(null)
            .build();

        // Calculate progress and completion status
        updateProgressAndCompletion(courseProgress);

        courseProgress.onCreate();
        courseProgressRepository.save(courseProgress);

        return mapToResponse(courseProgress);
    }

    @Override
    public CourseProgressResponseDto getCourseProgressById(UUID courseProgressId) {
        CourseProgress courseProgress = courseProgressRepository.findById(courseProgressId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException(
                "Course progress not found with ID: " + courseProgressId));
        return mapToResponse(courseProgress);
    }

    @Override
    public CourseProgressResponseDto getCourseProgressByEnrollmentId(UUID enrollmentId) {
        CourseProgress courseProgress = courseProgressRepository.findByEnrollmentId(enrollmentId);
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException(
                "Course progress not found for enrollment ID: " + enrollmentId);
        }
        return mapToResponse(courseProgress);
    }

    @Override
    public CourseProgressResponseDto updateCourseProgress(UUID courseProgressId, CourseProgressRequestDto request) {
        validateUpdateRequest(courseProgressId, request);

        CourseProgress courseProgress = courseProgressRepository.findById(courseProgressId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException(
                "Course progress not found with ID: " + courseProgressId));

        // Store original values for validation
        Integer originalLessonsCompleted = courseProgress.getLessonsCompleted();
        Integer originalTotalLessons = courseProgress.getTotalLessons();

        // Apply updates
        courseProgress.setLessonsCompleted(request.getLessonsCompleted());
        courseProgress.setTotalLessons(request.getTotalLessons());

        // Validate business rules
        validateProgressRules(courseProgress, originalLessonsCompleted, originalTotalLessons);

        // Recalculate progress and completion
        updateProgressAndCompletion(courseProgress);

        courseProgress.onUpdate();
        return mapToResponse(courseProgressRepository.save(courseProgress));
    }

    // PATCH: Update only provided fields
    @Override
    public CourseProgressResponseDto patchCourseProgress(UUID courseProgressId, CourseProgressRequestDto request) {
        validatePatchRequest(courseProgressId, request);

        CourseProgress courseProgress = courseProgressRepository.findById(courseProgressId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException(
                "Course progress not found with ID: " + courseProgressId));

        Integer originalLessonsCompleted = courseProgress.getLessonsCompleted();
        Integer originalTotalLessons = courseProgress.getTotalLessons();

        // Apply partial updates
        if (request.getLessonsCompleted() != null) {
            courseProgress.setLessonsCompleted(request.getLessonsCompleted());
        }
        if (request.getTotalLessons() != null) {
            courseProgress.setTotalLessons(request.getTotalLessons());
        }

        // Validate business rules for changed fields
        validateProgressRules(courseProgress, originalLessonsCompleted, originalTotalLessons);

        // Recalculate progress and completion
        updateProgressAndCompletion(courseProgress);

        courseProgress.onUpdate();
        return mapToResponse(courseProgressRepository.save(courseProgress));
    }

    // Update only lessons completed
    @Override
    public CourseProgressResponseDto updateLessonsCompleted(UUID courseProgressId, Integer lessonsCompleted) {
        if (courseProgressId == null) {
            throw new CourseProgressException.InvalidRequestException("Course progress ID cannot be null");
        }

        CourseProgress courseProgress = courseProgressRepository.findById(courseProgressId)
            .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException(
                "Course progress not found with ID: " + courseProgressId));

        Integer originalLessonsCompleted = courseProgress.getLessonsCompleted();
        courseProgress.setLessonsCompleted(lessonsCompleted);

        validateProgressRules(courseProgress, originalLessonsCompleted, courseProgress.getTotalLessons());
        updateProgressAndCompletion(courseProgress);

        courseProgress.onUpdate();
        return mapToResponse(courseProgressRepository.save(courseProgress));
    }

    // ========== Mapping ==========

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

    // ========== Validation ==========

    private void validateCreateRequest(CourseProgressRequestDto request) {
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }
        if (request.getEnrollmentId() == null) {
            throw new CourseProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        if (request.getLessonsCompleted() == null || request.getLessonsCompleted() < 0) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed must be non-negative");
        }
        if (request.getTotalLessons() == null || request.getTotalLessons() <= 0) {
            throw new CourseProgressException.InvalidRequestException("Total lessons must be positive");
        }
        if (request.getLessonsCompleted() > request.getTotalLessons()) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed cannot exceed total lessons");
        }
    }

    private void validateUpdateRequest(UUID courseProgressId, CourseProgressRequestDto request) {
        if (courseProgressId == null) {
            throw new CourseProgressException.InvalidRequestException("Course progress ID cannot be null");
        }
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }
        if (request.getLessonsCompleted() == null || request.getLessonsCompleted() < 0) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed must be non-negative");
        }
        if (request.getTotalLessons() == null || request.getTotalLessons() <= 0) {
            throw new CourseProgressException.InvalidRequestException("Total lessons must be positive");
        }
        if (request.getLessonsCompleted() > request.getTotalLessons()) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed cannot exceed total lessons");
        }
    }

    private void validatePatchRequest(UUID courseProgressId, CourseProgressRequestDto request) {
        if (courseProgressId == null) {
            throw new CourseProgressException.InvalidRequestException("Course progress ID cannot be null");
        }
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }

        boolean hasAny = request.getLessonsCompleted() != null || request.getTotalLessons() != null;
        if (!hasAny) {
            throw new CourseProgressException.InvalidRequestException("At least one field must be provided for update");
        }

        // Validate provided fields
        if (request.getLessonsCompleted() != null && request.getLessonsCompleted() < 0) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed must be non-negative");
        }
        if (request.getTotalLessons() != null && request.getTotalLessons() <= 0) {
            throw new CourseProgressException.InvalidRequestException("Total lessons must be positive");
        }
    }

    private void validateProgressRules(CourseProgress courseProgress, Integer originalLessonsCompleted, Integer originalTotalLessons) {
        // Lessons completed cannot exceed total lessons
        if (courseProgress.getLessonsCompleted() > courseProgress.getTotalLessons()) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed cannot exceed total lessons");
        }

        // Business rule: Progress cannot decrease significantly (allow small adjustments)
        if (courseProgress.getLessonsCompleted() < originalLessonsCompleted - 1) {
            throw new CourseProgressException.InvalidRequestException(
                "Progress cannot decrease significantly. Original: " + originalLessonsCompleted + 
                ", New: " + courseProgress.getLessonsCompleted());
        }

        // If total lessons decreased, ensure lessons completed doesn't exceed new total
        if (courseProgress.getTotalLessons() < originalTotalLessons) {
            if (courseProgress.getLessonsCompleted() > courseProgress.getTotalLessons()) {
                throw new CourseProgressException.InvalidRequestException(
                    "Cannot reduce total lessons below current completed lessons");
            }
        }
    }

    // ========== Business Logic ==========

    private void updateProgressAndCompletion(CourseProgress courseProgress) {
        // Calculate progress as percentage (0.0 to 1.0)
        if (courseProgress.getTotalLessons() > 0) {
            double progress = (double) courseProgress.getLessonsCompleted() / courseProgress.getTotalLessons();
            courseProgress.setOverallProgress(Math.round(progress * 100.0) / 100.0); // Round to 2 decimal places
        } else {
            courseProgress.setOverallProgress(0.0);
        }

        // Update completion status
        boolean isCompleted = courseProgress.getOverallProgress() >= 1.0;
        courseProgress.setCourseCompleted(isCompleted);
        
        if (isCompleted && courseProgress.getCourseCompletedAt() == null) {
            courseProgress.setCourseCompletedAt(LocalDateTime.now());
        } else if (!isCompleted) {
            courseProgress.setCourseCompletedAt(null);
        }
    }
}