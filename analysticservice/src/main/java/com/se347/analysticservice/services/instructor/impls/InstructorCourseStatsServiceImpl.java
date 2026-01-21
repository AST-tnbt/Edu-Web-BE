package com.se347.analysticservice.services.instructor.impls;

import com.se347.analysticservice.domains.services.instructor.InstructorCourseStatsDomainService;
import com.se347.analysticservice.domains.services.overview.OverviewSynchronizationService;
import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.InstructorCourseStatsRepository;
import com.se347.analysticservice.services.instructor.InstructorCourseStatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for Instructor Course Stats.
 * 
 * DDD PATTERN: Application Service
 * 
 * RESPONSIBILITIES:
 * - Orchestrate use cases (record enrollment, revenue, update completion rate)
 * - Handle transaction boundaries
 * - Coordinate between repositories and domain services
 * 
 * BUSINESS LOGIC:
 * - Delegated to InstructorCourseStatsDomainService (factory logic)
 * - Delegated to OverviewSynchronizationService (cross-aggregate synchronization)
 * - Entity business methods are called on entities
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorCourseStatsServiceImpl implements InstructorCourseStatsService {
    
    private final InstructorCourseStatsRepository courseStatsRepository;
    private final InstructorCourseStatsDomainService domainService;
    private final OverviewSynchronizationService overviewSynchronizationService;
    
    @Override
    @Transactional
    public void recordEnrollment(UUID instructorId, UUID courseId, Count count) {
        log.debug("Recording enrollment for course: instructorId={}, courseId={}, count={}", 
            instructorId, courseId, count.getValue());
        
        InstructorCourseStats courseStats = getOrCreate(instructorId, courseId);
        
        courseStats.recordEnrollment(count);
        courseStatsRepository.save(courseStats);
    }
    
    @Override
    @Transactional
    public void updateOverallProgress(UUID instructorId, UUID courseId, UUID enrollmentId, double newOverallProgress) {
        log.debug("Updating overall progress for course: instructorId={}, courseId={}, enrollmentId={}, newOverallProgress={}", 
            instructorId, courseId, enrollmentId, newOverallProgress);
        
        InstructorCourseStats courseStats = getOrCreate(instructorId, courseId);
        // newOverallProgress is expected to be a bounded percentage (0-100)
        courseStats.updateCompletionRate(Percentage.ofBounded(newOverallProgress));
        courseStatsRepository.save(courseStats);

        // Synchronize instructor overview via Domain Service
        // NOTE: This ensures consistency, but may be redundant if called from event listener
        // The Domain Service will handle the synchronization logic
        overviewSynchronizationService.synchronizeInstructorOverview(instructorId);
    }

    public void recordRevenue(UUID instructorId, UUID courseId, Money amount) {
        log.debug("Recording revenue for course: instructorId={}, courseId={}, amount={}", 
            instructorId, courseId, amount.getAmount());
        
        InstructorCourseStats courseStats = getOrCreate(instructorId, courseId);
        
        courseStats.recordRevenue(amount);
        courseStatsRepository.save(courseStats);
    }
    
    @Override
    @Transactional
    public void updateCompletionRate(UUID instructorId, UUID courseId, Percentage completionRate) {
        log.debug("Updating course completion rate: instructorId={}, courseId={}, completionRate={}", 
            instructorId, courseId, completionRate.getValue());
        
        InstructorCourseStats courseStats = getOrCreate(instructorId, courseId);
        
        courseStats.updateCompletionRate(completionRate);
        courseStatsRepository.save(courseStats);
        
        // Synchronize instructor overview via Domain Service
        overviewSynchronizationService.synchronizeInstructorOverview(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorCourseStats> getByInstructorIdAndCourseId(UUID instructorId, UUID courseId) {
        return courseStatsRepository.findByInstructorIdAndCourseId(instructorId, courseId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourseStats> getByInstructorId(UUID instructorId) {
        return courseStatsRepository.findByInstructorId(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourseStats> getByInstructorIdOrderByRevenueDesc(UUID instructorId) {
        return courseStatsRepository.findByInstructorIdOrderByRevenueDesc(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourseStats> getByInstructorIdOrderByCompletionRateDesc(UUID instructorId) {
        return courseStatsRepository.findByInstructorIdOrderByCompletionRateDesc(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public InstructorCourseStats getOrCreate(UUID instructorId, UUID courseId) {
        return courseStatsRepository.findByInstructorIdAndCourseId(instructorId, courseId)
            .orElseGet(() -> domainService.createInitialCourseStats(instructorId, courseId));
    }
    
    @Override
    @Transactional
    public void ensureCourseStatsExists(UUID instructorId, UUID courseId) {
        if (!courseStatsRepository.existsByInstructorIdAndCourseId(instructorId, courseId)) {
            // Create using domain service (contains business rules)
            InstructorCourseStats courseStats = domainService.createInitialCourseStats(instructorId, courseId);
            courseStatsRepository.save(courseStats);
        }
    }
}
