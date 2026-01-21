package com.se347.analysticservice.domains.services.platform;

import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.UserGrowthAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Domain Service for UserGrowthAnalytics aggregate.
 * 
 * DDD PATTERN: Domain Service
 * 
 * RESPONSIBILITIES:
 * - Factory logic for creating UserGrowthAnalytics with proper initial values
 * - Business rules for initializing analytics records
 * - Cross-aggregate logic for user growth analytics
 * 
 * USAGE:
 * This service is called by Application Services when they need to create
 * or initialize UserGrowthAnalytics entities with proper business rules.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserGrowthAnalyticsDomainService {
    
    private final UserGrowthAnalyticsRepository repository;
    private final PlatformMetricsAggregationService aggregationService;
    
    /**
     * Creates initial UserGrowthAnalytics record for a date.
     * 
     * BUSINESS RULE:
     * - If this is the first record in the system and being created during user registration,
     *   initialize with newUsersCount=1, totalUsers=1
     * - Otherwise, get totalUsers from the most recent record
     * 
     * @param date the date for the analytics record
     * @param isFirstRecord true if this is the very first analytics record in the system
     * @return new UserGrowthAnalytics entity with proper initial values
     */
    @Transactional(readOnly = true)
    public UserGrowthAnalytics createInitialAnalytics(LocalDate date, boolean isFirstRecord) {
        Count totalUsers;
        Count newUsersCount;
        
        if (isFirstRecord) {
            // Đây là record đầu tiên và đang trong context của user registration
            // Khởi tạo với newUsersCount = 1, totalUsers = 1 (user đang được record)
            totalUsers = Count.of(1);
            newUsersCount = Count.of(1);
            log.info("Creating first UserGrowthAnalytics record for date={} with initial user registration", date);
        } else {
            // Đã có analytics record trước đó, lấy totalUsers từ record gần nhất
            totalUsers = aggregationService.getMostRecentTotalUsers();
            newUsersCount = Count.zero();
            log.debug("Creating new UserGrowthAnalytics record for date={} with totalUsers={} from previous record", 
                     date, totalUsers.getValue());
        }
        
        return UserGrowthAnalytics.create(
            date,
            newUsersCount,
            Count.zero(), // activeUsersCount = 0 (sẽ được update khi user login)
            totalUsers,
            Percentage.zero() // retentionRate = 0 (sẽ được tính sau)
        );
    }
    
    /**
     * Creates initial analytics with default values (for non-registration contexts).
     * 
     * @param date the date for the analytics record
     * @return new UserGrowthAnalytics entity with default values
     */
    @Transactional(readOnly = true)
    public UserGrowthAnalytics createInitialAnalytics(LocalDate date) {
        return createInitialAnalytics(date, false);
    }
    
    /**
     * Determines if a newly created analytics record should have recordNewUser() called.
     * 
     * BUSINESS RULE:
     * - If record was just created as first record with newUsersCount=1, totalUsers=1,
     *   then don't call recordNewUser() again (already initialized correctly)
     * - Otherwise, call recordNewUser() to increment
     * 
     * @param analytics the analytics record to check
     * @param isFirstRecord true if this was the first record in the system
     * @return true if recordNewUser() should be called, false otherwise
     */
    public boolean shouldRecordNewUser(UserGrowthAnalytics analytics, boolean isFirstRecord) {
        // Nếu đây là record đầu tiên và vừa được tạo với newUsersCount=1, totalUsers=1
        // thì không cần gọi recordNewUser() nữa vì đã được khởi tạo đúng rồi
        return !(isFirstRecord && 
                analytics.getNewUsersCount().getValue() == 1 && 
                analytics.getTotalUsers().getValue() == 1);
    }
    
    /**
     * Checks if this is the first analytics record in the system.
     * 
     * @return true if no analytics records exist, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isFirstRecord() {
        return !repository.findMostRecent().isPresent();
    }
    
    /**
     * Calculates and updates retention rate for a date.
     * 
     * BUSINESS RULE:
     * - Retention rate = (today's active users / yesterday's active users) * 100%
     * - If yesterday's analytics doesn't exist, skip calculation
     * 
     * @param date the date to calculate retention for
     * @return true if retention was calculated and updated, false otherwise
     */
    @Transactional
    public boolean calculateAndUpdateRetention(LocalDate date) {
        LocalDate yesterday = date.minusDays(1);
        
        Optional<UserGrowthAnalytics> yesterdayOpt = repository.findByDate(yesterday);
        if (yesterdayOpt.isEmpty()) {
            log.debug("Cannot calculate retention for date={}: yesterday's analytics not found", date);
            return false;
        }
        
        Optional<UserGrowthAnalytics> todayOpt = repository.findByDate(date);
        if (todayOpt.isEmpty()) {
            log.debug("Cannot calculate retention for date={}: today's analytics not found", date);
            return false;
        }
        
        UserGrowthAnalytics yesterdayAnalytics = yesterdayOpt.get();
        UserGrowthAnalytics todayAnalytics = todayOpt.get();
        
        Percentage retention = todayAnalytics.calculateRetentionRate(
            yesterdayAnalytics.getActiveUsersCount()
        );
        
        todayAnalytics.updateRetentionRate(retention);
        repository.save(todayAnalytics);
        
        log.debug("Retention rate calculated for date={}: {}%", date, retention.getValue());
        return true;
    }
}
