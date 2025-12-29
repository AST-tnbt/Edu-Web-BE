package com.se347.analysticservice.services.impls;

import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.UserGrowthAnalyticsRepository;
import com.se347.analysticservice.services.UserGrowthAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserGrowthAnalyticsServiceImpl implements UserGrowthAnalyticsService {
    
    private final UserGrowthAnalyticsRepository analyticsRepository;
    
    /**
     * Records a new user registration.
     * Business flow:
     * 1. Load or create analytics for registration date
     * 2. Call domain method to record new user
     * 3. Save aggregate (domain events auto-published)
     */
    @Override
    public void recordUserRegistration(UUID userId, LocalDate registrationDate) {
        
        try {
            // Load or create analytics for the date
            UserGrowthAnalytics analytics = analyticsRepository.findByDate(registrationDate)
                .orElseGet(() -> createInitialAnalytics(registrationDate));
            
            // Domain method: Record new user (business logic in domain)
            analytics.recordNewUser();
            
            // Save aggregate (triggers domain event publication)
            analyticsRepository.save(analytics);
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to record user registration", e);
        }
    }
    
    /**
     * Records user activity (login, course view, etc.).
     * Business flow:
     * 1. Load or create analytics for activity date
     * 2. Call domain method to record active user
     * 3. Save aggregate
     */
    @Override
    public void recordUserActivity(UUID userId, LocalDate activityDate) {

        try {
            // Load or create analytics for the date
            UserGrowthAnalytics analytics = analyticsRepository.findByDate(activityDate)
                .orElseGet(() -> createInitialAnalytics(activityDate));
            
            // Domain method: Record active user
            analytics.recordActiveUser();
            
            // Save aggregate
            analyticsRepository.save(analytics);

        } catch (Exception e) {
            throw new RuntimeException("Failed to record user activity", e);
        }
    }
    
    /**
     * Calculates daily retention rate.
     * Business flow:
     * 1. Get yesterday's analytics
     * 2. Get today's analytics
     * 3. Calculate retention = (today active / yesterday active) * 100%
     * 4. Update today's retention rate
     */
    @Override
    public void calculateDailyRetention(LocalDate date) {
        
        try {
            LocalDate yesterday = date.minusDays(1);
            
            // Get yesterday's analytics
            UserGrowthAnalytics yesterdayAnalytics = analyticsRepository.findByDate(yesterday)
                .orElse(null);
            
            if (yesterdayAnalytics == null) {
                return;
            }
            
            // Get today's analytics
            UserGrowthAnalytics todayAnalytics = analyticsRepository.findByDate(date)
                .orElse(null);
            
            if (todayAnalytics == null) {
                return;
            }
            
            // Calculate retention using domain method
            Percentage retention = todayAnalytics.calculateRetentionRate(
                yesterdayAnalytics.getActiveUsersCount()
            );
            
            // Update retention rate
            todayAnalytics.updateRetentionRate(retention);
            
            // Save
            analyticsRepository.save(todayAnalytics);
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate daily retention", e);
        }
    }
    
    /**
     * Initializes analytics for a new day.
     * Gets previous day's total users as starting point.
     */
    @Override
    public Count initializeAnalyticsForDate(LocalDate date) {
        
        // Get most recent analytics to get total users
        UserGrowthAnalytics previousAnalytics = analyticsRepository.findMostRecent()
            .orElse(null);
        
        Count totalUsers = (previousAnalytics != null) 
            ? previousAnalytics.getTotalUsers() 
            : Count.zero();
        
        return totalUsers;
    }
    
    /**
     * Creates initial analytics record for a date.
     * Private helper method for internal use.
     */
    private UserGrowthAnalytics createInitialAnalytics(LocalDate date) {
        
        // Get total users from most recent analytics
        Count totalUsers = initializeAnalyticsForDate(date);
        
        // Create new analytics with zero counts
        UserGrowthAnalytics analytics = UserGrowthAnalytics.create(
            date,
            Count.zero(),      // newUsersCount - will be incremented
            Count.zero(),      // activeUsersCount - will be incremented
            totalUsers,        // totalUsers from previous day
            Percentage.zero()  // retentionRate - will be calculated later
        );
        
        return analytics;
    }
}

