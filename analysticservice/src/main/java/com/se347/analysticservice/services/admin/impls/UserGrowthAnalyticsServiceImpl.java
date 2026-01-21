package com.se347.analysticservice.services.admin.impls;

import com.se347.analysticservice.domains.services.platform.PlatformMetricsAggregationService;
import com.se347.analysticservice.domains.services.platform.UserGrowthAnalyticsDomainService;
import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.UserGrowthAnalyticsRepository;
import com.se347.analysticservice.services.admin.UserGrowthAnalyticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for UserGrowthAnalytics.
 * 
 * DDD PATTERN: Application Service
 * 
 * RESPONSIBILITIES:
 * - Orchestrate use cases (record user registration, activity, etc.)
 * - Handle transaction boundaries
 * - Coordinate between repositories and domain services
 * - Handle infrastructure concerns (locking, retry logic)
 * 
 * BUSINESS LOGIC:
 * - Delegated to UserGrowthAnalyticsDomainService
 * - Entity business methods (recordNewUser, recordActiveUser) are called on entities
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserGrowthAnalyticsServiceImpl implements UserGrowthAnalyticsService {
    
    private final UserGrowthAnalyticsRepository repository;
    private final UserGrowthAnalyticsDomainService domainService;
    private final PlatformMetricsAggregationService aggregationService;
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordUserRegistration(UUID userId, LocalDate registrationDate) {
        // Check if this is the first record (before lock to avoid unnecessary locking)
        boolean isFirstRecord = domainService.isFirstRecord();
        
        // Find or create analytics record with pessimistic lock
        UserGrowthAnalytics analytics = repository
            .findByDateWithLock(registrationDate)
            .orElseGet(() -> {
                // Create new record using domain service (contains business rules)
                UserGrowthAnalytics newAnalytics = domainService.createInitialAnalytics(registrationDate, isFirstRecord);
                try {
                    return repository.save(newAnalytics);
                } catch (DataIntegrityViolationException e) {
                    // Race condition: another thread created it, retry with lock
                    log.warn(
                        "Duplicate key detected when creating UserGrowthAnalytics, retrying with lock: date={}",
                        registrationDate
                    );
                    return repository.findByDateWithLock(registrationDate)
                        .orElseThrow(() -> new IllegalStateException(
                            "Failed to create or find UserGrowthAnalytics for date: " + registrationDate
                        ));
                }
            });

        // Use domain service to determine if we should record new user
        // (business rule: first record already initialized with user=1)
        if (domainService.shouldRecordNewUser(analytics, isFirstRecord)) {
            analytics.recordNewUser();
        }
        
        repository.save(analytics);
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void recordUserActivity(UUID userId, LocalDate activityDate) {
        // Find or create analytics record with pessimistic lock
        UserGrowthAnalytics analytics = repository.findByDateWithLock(activityDate)
            .orElseGet(() -> {
                // Create new record using domain service (for non-registration context)
                UserGrowthAnalytics newAnalytics = domainService.createInitialAnalytics(activityDate);
                try {
                    return repository.save(newAnalytics);
                } catch (DataIntegrityViolationException e) {
                    // Race condition: another thread created it, retry with lock
                    log.warn(   
                        "Duplicate key detected when creating UserGrowthAnalytics, retrying with lock: date={}",
                        activityDate
                    );
                    return repository.findByDateWithLock(activityDate)
                        .orElseThrow(() -> new IllegalStateException(
                            "Failed to create or find UserGrowthAnalytics for date: " + activityDate
                        ));
                }
            });

        // Call entity business method
        analytics.recordActiveUser();
        repository.save(analytics);
    }
    
    @Override
    @Transactional
    public void calculateDailyRetention(LocalDate date) {
        // Delegate to domain service (contains business rules for retention calculation)
        domainService.calculateAndUpdateRetention(date);
    }
    
    @Override
    @Transactional
    public Count initializeAnalyticsForDate(LocalDate date) {
        // Delegate to aggregation service (cross-aggregate query)
        return aggregationService.getMostRecentTotalUsers();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserGrowthAnalytics> getAnalyticsForDate(LocalDate date) {
        return repository.findByDate(date);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserGrowthAnalytics> getAnalyticsForPeriod(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateBetween(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Percentage getAverageRetentionRate(LocalDate startDate, LocalDate endDate) {
        return aggregationService.getAverageRetentionInPeriod(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Count getTotalActiveUsers(LocalDate date) {
        return repository.findByDate(date)
            .map(UserGrowthAnalytics::getActiveUsersCount)
            .orElse(Count.zero());
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserGrowthAnalytics getMostRecentAnalytics() {
        return repository.findMostRecent()
            .orElseThrow(() -> new IllegalStateException("No analytics data available"));
    }
    
}

