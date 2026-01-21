package com.se347.analysticservice.services.instructor.impls;

import com.se347.analysticservice.domains.services.instructor.InstructorOverviewDomainService;
import com.se347.analysticservice.domains.services.overview.OverviewSynchronizationService;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.InstructorOverviewRepository;
import com.se347.analysticservice.services.instructor.InstructorOverviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for Instructor Overview.
 * 
 * DDD PATTERN: Application Service
 * 
 * RESPONSIBILITIES:
 * - Orchestrate use cases (record course, enrollment, revenue, etc.)
 * - Handle transaction boundaries
 * - Coordinate between repositories and domain services
 * - Handle infrastructure concerns (locking, retry logic)
 * 
 * BUSINESS LOGIC:
 * - Delegated to InstructorOverviewDomainService (factory and calculation logic)
 * - Delegated to OverviewSynchronizationService (cross-aggregate synchronization)
 * - Entity business methods are called on entities
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorOverviewServiceImpl implements InstructorOverviewService {
    
    private final InstructorOverviewRepository overviewRepository;
    private final InstructorOverviewDomainService domainService;
    private final OverviewSynchronizationService overviewSynchronizationService;
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordCourse(UUID instructorId, UUID courseId) {
        log.debug("Recording course for instructor overview: instructorId={}, courseId={}", instructorId, courseId);
        
        InstructorOverview overview = getOrCreateWithLock(instructorId);
        
        overview.updateMetrics(
            overview.getTotalCourses().increment(),
            overview.getTotalStudents(),
            overview.getTotalRevenue(),
            overview.getAverageCompletionRate()
        );
        
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordEnrollment(UUID instructorId, Count count) {
        log.debug("Recording enrollment for instructor overview: instructorId={}, count={}", instructorId, count.getValue());
        
        InstructorOverview overview = getOrCreateWithLock(instructorId);
        
        overview.updateMetrics(
            overview.getTotalCourses(),
            overview.getTotalStudents().add(count),
            overview.getTotalRevenue(),
            overview.getAverageCompletionRate()
        );
        
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordRevenue(UUID instructorId, Money amount) {
        log.debug("Recording revenue for instructor overview: instructorId={}, amount={}", instructorId, amount.getAmount());
        
        InstructorOverview overview = getOrCreateWithLock(instructorId);
        
        overview.updateMetrics(
            overview.getTotalCourses(),
            overview.getTotalStudents(),
            overview.getTotalRevenue().add(amount),
            overview.getAverageCompletionRate()
        );
        
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional
    public void recalculateAverageCompletionRate(UUID instructorId) {
        log.debug("Recalculating average completion rate for instructor overview: instructorId={}", instructorId);
        
        InstructorOverview overview = getOrCreate(instructorId);
        
        // Delegate to domain service (contains business rules for calculation)
        Percentage averageCompletionRate = domainService.calculateAverageCompletionRate(instructorId);
        overview.updateAverageCompletionRate(averageCompletionRate);
        
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorOverview> getByInstructorId(UUID instructorId) {
        return overviewRepository.findByInstructorId(instructorId);
    }
    
    @Override
    @Transactional
    public void recordEnrollmentCompletionRateUpdate(
        UUID instructorId, 
        UUID courseId, 
        UUID enrollmentId,
        Double previousEnrollmentRate, 
        Double newEnrollmentRate
    ) {
        log.debug("Recording enrollment completion rate update: instructorId={}, courseId={}, enrollmentId={}, " +
                  "previousRate={}, newRate={}", 
            instructorId, courseId, enrollmentId, previousEnrollmentRate, newEnrollmentRate);
        
        InstructorOverview overview = getOrCreate(instructorId);
        
        overview.recordEnrollmentCompletionRateUpdate(courseId, enrollmentId, previousEnrollmentRate, newEnrollmentRate);
        overviewRepository.save(overview);
    }
    
    /**
     * Gets or creates InstructorOverview with pessimistic locking to prevent race conditions.
     * Uses PESSIMISTIC_WRITE lock to ensure only one thread can create the record.
     * Note: This method should be called from within a transaction with REPEATABLE_READ isolation.
     */
    private InstructorOverview getOrCreateWithLock(UUID instructorId) {
        // Try to get existing record with PESSIMISTIC_WRITE lock
        return overviewRepository.findByInstructorIdWithLock(instructorId)
            .orElseGet(() -> {
                // Create new record using domain service (contains business rules)
                InstructorOverview newOverview = domainService.createInitialOverview(instructorId);
                try {
                    return overviewRepository.save(newOverview);
                } catch (DataIntegrityViolationException e) {
                    // Race condition: another thread created it, retry with lock
                    log.warn(
                        "Duplicate key detected when creating InstructorOverview, retrying with lock: instructorId={}",
                        instructorId
                    );
                    return overviewRepository.findByInstructorIdWithLock(instructorId)
                        .orElseThrow(() -> new IllegalStateException(
                            "Failed to create or find InstructorOverview for instructorId: " + instructorId
                        ));
                }
            });
    }
    
    @Transactional(readOnly = true)
    public InstructorOverview getOrCreate(UUID instructorId) {
        return overviewRepository.findByInstructorId(instructorId)
            .orElseGet(() -> domainService.createInitialOverview(instructorId));
    }
    
    @Override
    @Transactional
    public void recalculateInstructorOverview(UUID instructorId) {
        log.info("Recalculating instructor overview from related entities: instructorId={}", instructorId);
        
        // Delegate to domain service for synchronization
        // DDD PATTERN: Domain Service handles cross-aggregate synchronization logic
        overviewSynchronizationService.synchronizeInstructorOverview(instructorId);
    }
    
}
