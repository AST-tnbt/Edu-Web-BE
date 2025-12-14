package com.se347.enrollmentservice.domains.impls;

import com.se347.enrollmentservice.domains.CourseProgressDomainService;
import com.se347.enrollmentservice.dtos.CourseProgressRequestDto;
import com.se347.enrollmentservice.entities.CourseProgress;
import com.se347.enrollmentservice.exceptions.CourseProgressException;
import com.se347.enrollmentservice.repositories.CourseProgressRepository;
import com.se347.enrollmentservice.repositories.EnrollmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseProgressDomainServiceImpl implements CourseProgressDomainService {
    
    private final CourseProgressRepository courseProgressRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ========== CourseProgress Entity CRUD operations ==========

    @Override
    @Transactional(readOnly = true)
    public CourseProgress findCourseProgressById(UUID courseProgressId) {
        if (courseProgressId == null) {
            throw new CourseProgressException.InvalidRequestException("Course progress ID cannot be null");
        }
        return courseProgressRepository.findById(courseProgressId)
                .orElseThrow(() -> new CourseProgressException.CourseProgressNotFoundException(
                    "Course progress not found with ID: " + courseProgressId));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgress findCourseProgressByEnrollmentId(UUID enrollmentId) {
        if (enrollmentId == null) {
            throw new CourseProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        CourseProgress courseProgress = courseProgressRepository.findByEnrollmentId(enrollmentId);
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException(
                "Course progress not found for enrollment ID: " + enrollmentId);
        }
        return courseProgress;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean courseProgressExists(UUID courseProgressId) {
        if (courseProgressId == null) {
            return false;
        }
        return courseProgressRepository.existsById(courseProgressId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean courseProgressExistsByEnrollmentId(UUID enrollmentId) {
        if (enrollmentId == null) {
            return false;
        }
        return courseProgressRepository.findByEnrollmentId(enrollmentId) != null;
    }

    // ========== Entity operations ==========

    @Override
    public CourseProgress createCourseProgressEntity(CourseProgressRequestDto request) {
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
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

        return courseProgress;
    }

    @Override
    public CourseProgress updateCourseProgressEntity(CourseProgress courseProgress, CourseProgressRequestDto request) {
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException("Course progress cannot be null");
        }
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }

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

        return courseProgress;
    }

    @Override
    public CourseProgress patchCourseProgressEntity(CourseProgress courseProgress, CourseProgressRequestDto request) {
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException("Course progress cannot be null");
        }
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }

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

        return courseProgress;
    }

    @Override
    public CourseProgress setTotalLessonsEntity(CourseProgress courseProgress, Integer totalLessons) {
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException("Course progress cannot be null");
        }
        if (totalLessons == null || totalLessons < 0) {
            throw new CourseProgressException.InvalidRequestException("Total lessons must be non-negative");
        }

        // Validate that lessonsCompleted doesn't exceed new totalLessons
        if (courseProgress.getLessonsCompleted() > totalLessons) {
            throw new CourseProgressException.InvalidRequestException(
                "Cannot set totalLessons to " + totalLessons + 
                " because lessonsCompleted (" + courseProgress.getLessonsCompleted() + ") exceeds it");
        }

        courseProgress.setTotalLessons(totalLessons);

        // Recalculate progress and completion status after totalLessons change
        updateProgressAndCompletion(courseProgress);
        courseProgress.onUpdate();

        return courseProgress;
    }

    // ========== Business validations ==========

    @Override
    public void validateCourseProgressCreation(CourseProgressRequestDto request) {
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }
        if (request.getEnrollmentId() == null) {
            throw new CourseProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        if (request.getLessonsCompleted() == null || request.getLessonsCompleted() < 0) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed must be non-negative");
        }
        if (request.getTotalLessons() == null || request.getTotalLessons() < 0) {
            throw new CourseProgressException.InvalidRequestException("Total lessons must be non-negative");
        }
        if (request.getLessonsCompleted() > request.getTotalLessons()) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed cannot exceed total lessons");
        }
        
        // Verify enrollment exists
        validateEnrollmentExists(request.getEnrollmentId());
    }

    @Override
    public void validateCourseProgressUpdate(CourseProgress courseProgress, CourseProgressRequestDto request) {
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException("Course progress cannot be null");
        }
        if (request == null) {
            throw new CourseProgressException.InvalidRequestException("Request cannot be null");
        }
        if (request.getLessonsCompleted() == null || request.getLessonsCompleted() < 0) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed must be non-negative");
        }
        if (request.getTotalLessons() == null || request.getTotalLessons() < 0) {
            throw new CourseProgressException.InvalidRequestException("Total lessons must be non-negative");
        }
        if (request.getLessonsCompleted() > request.getTotalLessons()) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed cannot exceed total lessons");
        }
    }

    @Override
    public void validateProgressRules(CourseProgress courseProgress, Integer originalLessonsCompleted, Integer originalTotalLessons) {
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException("Course progress cannot be null");
        }

        // Lessons completed cannot exceed total lessons
        if (courseProgress.getLessonsCompleted() > courseProgress.getTotalLessons()) {
            throw new CourseProgressException.InvalidRequestException("Lessons completed cannot exceed total lessons");
        }

        // Business rule: Progress cannot decrease significantly (allow small adjustments)
        if (originalLessonsCompleted != null && courseProgress.getLessonsCompleted() < originalLessonsCompleted - 1) {
            throw new CourseProgressException.InvalidRequestException(
                "Progress cannot decrease significantly. Original: " + originalLessonsCompleted + 
                ", New: " + courseProgress.getLessonsCompleted());
        }

        // If total lessons decreased, ensure lessons completed doesn't exceed new total
        if (originalTotalLessons != null && courseProgress.getTotalLessons() < originalTotalLessons) {
            if (courseProgress.getLessonsCompleted() > courseProgress.getTotalLessons()) {
                throw new CourseProgressException.InvalidRequestException(
                    "Cannot reduce total lessons below current completed lessons");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateEnrollmentExists(UUID enrollmentId) {
        if (enrollmentId == null) {
            throw new CourseProgressException.InvalidRequestException("Enrollment ID cannot be null");
        }
        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new CourseProgressException.InvalidRequestException(
                "Enrollment not found with ID: " + enrollmentId);
        }
    }

    // ========== Business logic ==========

    @Override
    public void calculateOverallProgress(CourseProgress courseProgress) {
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException("Course progress cannot be null");
        }

        // Calculate progress as percentage (0.0 to 1.0)
        if (courseProgress.getTotalLessons() > 0) {
            double progress = (double) courseProgress.getLessonsCompleted() / courseProgress.getTotalLessons();
            courseProgress.setOverallProgress(Math.round(progress * 100.0) / 100.0); // Round to 2 decimal places
        } else {
            courseProgress.setOverallProgress(0.0);
        }
    }

    @Override
    public void updateProgressAndCompletion(CourseProgress courseProgress) {
        if (courseProgress == null) {
            throw new CourseProgressException.CourseProgressNotFoundException("Course progress cannot be null");
        }

        // Calculate overall progress
        calculateOverallProgress(courseProgress);

        // Update completion status
        boolean isCompleted = courseProgress.getOverallProgress() >= 1.0;
        courseProgress.setCourseCompleted(isCompleted);
        
        if (isCompleted && courseProgress.getCourseCompletedAt() == null) {
            courseProgress.setCourseCompletedAt(LocalDateTime.now());
        } else if (!isCompleted) {
            courseProgress.setCourseCompletedAt(null);
        }
        courseProgress.onUpdate();
    }

    @Override
    public boolean canUserAccessCourseProgress(CourseProgress courseProgress, UUID userId) {
        if (courseProgress == null || userId == null) {
            return false;
        }
        return enrollmentRepository.findById(courseProgress.getEnrollmentId()).get().getStudentId().equals(userId);
    }
}

