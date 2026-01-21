package com.se347.analysticservice.services.instructor.impls;

import com.se347.analysticservice.domains.services.instructor.InstructorDailyStatsDomainService;
import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.repositories.InstructorDailyStatsRepository;
import com.se347.analysticservice.services.instructor.InstructorDailyStatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for Instructor Daily Stats.
 * 
 * DDD PATTERN: Application Service
 * 
 * RESPONSIBILITIES:
 * - Orchestrate use cases (record enrollment, revenue, active students, etc.)
 * - Handle transaction boundaries
 * - Coordinate between repositories and domain services
 * - Handle infrastructure concerns (locking, retry logic)
 * 
 * BUSINESS LOGIC:
 * - Delegated to InstructorDailyStatsDomainService (factory logic)
 * - Entity business methods are called on entities
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorDailyStatsServiceImpl implements InstructorDailyStatsService {
    
    private final InstructorDailyStatsRepository dailyStatsRepository;
    private final InstructorDailyStatsDomainService domainService;
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordEnrollment(UUID instructorId, LocalDate date) {
        log.debug("Recording enrollment for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = getOrCreateWithLock(instructorId, date);
        
        dailyStats.recordEnrollment();
        dailyStatsRepository.save(dailyStats);
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordActiveStudent(UUID instructorId, LocalDate date) {
        log.debug("Recording active student for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = getOrCreateWithLock(instructorId, date);
        
        dailyStats.recordActiveStudent();
        dailyStatsRepository.save(dailyStats);
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordRevenue(UUID instructorId, LocalDate date, Money amount) {
        log.debug("Recording revenue for daily stats: instructorId={}, date={}, amount={}", 
            instructorId, date, amount.getAmount());
        
        InstructorDailyStats dailyStats = getOrCreateWithLock(instructorId, date);
        
        dailyStats.recordRevenue(amount);
        dailyStatsRepository.save(dailyStats);
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordCourseCompletion(UUID instructorId, LocalDate date) {
        log.debug("Recording course completion for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = getOrCreateWithLock(instructorId, date);
        
        dailyStats.recordCourseCompletion();
        dailyStatsRepository.save(dailyStats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorDailyStats> getByInstructorIdAndDate(UUID instructorId, LocalDate date) {
        return dailyStatsRepository.findByInstructorIdAndDate(instructorId, date);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorDailyStats> getByInstructorId(UUID instructorId) {
        return dailyStatsRepository.findByInstructorId(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorDailyStats> getByInstructorIdAndDateBetween(UUID instructorId, LocalDate startDate, LocalDate endDate) {
        return dailyStatsRepository.findByInstructorIdAndDateBetween(instructorId, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorDailyStats> getByInstructorIdOrderByDateDesc(UUID instructorId) {
        return dailyStatsRepository.findByInstructorIdOrderByDateDesc(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public InstructorDailyStats getOrCreate(UUID instructorId, LocalDate date) {
        return dailyStatsRepository.findByInstructorIdAndDate(instructorId, date)
            .orElseGet(() -> domainService.createInitialDailyStats(instructorId, date));
    }
    
    /**
     * Gets or creates InstructorDailyStats with pessimistic locking to prevent race conditions.
     * Uses PESSIMISTIC_WRITE lock to ensure only one thread can create the record.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    private InstructorDailyStats getOrCreateWithLock(UUID instructorId, LocalDate date) {
        // Try to get existing record with PESSIMISTIC_WRITE lock
        return dailyStatsRepository.findByInstructorIdAndDateWithLock(instructorId, date)
            .orElseGet(() -> {
                // Create new record using domain service (contains business rules)
                InstructorDailyStats newStats = domainService.createInitialDailyStats(instructorId, date);
                try {
                    return dailyStatsRepository.save(newStats);
                } catch (DataIntegrityViolationException e) {
                    // Race condition: another thread created it, retry with lock
                    log.warn(
                        "Duplicate key detected when creating InstructorDailyStats, retrying with lock: instructorId={}, date={}",
                        instructorId, date
                    );
                    return dailyStatsRepository.findByInstructorIdAndDateWithLock(instructorId, date)
                        .orElseThrow(() -> new IllegalStateException(
                            "Failed to create or find InstructorDailyStats for instructorId: " + instructorId + ", date: " + date
                        ));
                }
            });
    }
}
