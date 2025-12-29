package com.se347.analysticservice.services;

import com.se347.analysticservice.entities.shared.valueobjects.Count;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Application Service for User Growth Analytics.
 * Orchestrates business workflows for tracking user registration and activity.
 */
public interface UserGrowthAnalyticsService {
    
    /**
     * Records a new user registration.
     * Creates or updates analytics record for the registration date.
     * 
     * @param userId ID of the registered user
     * @param registrationDate Date of registration
     */
    void recordUserRegistration(UUID userId, LocalDate registrationDate);
    
    /**
     * Records user activity (login).
     * Updates active users count for the activity date.
     * 
     * @param userId ID of the active user
     * @param activityDate Date of activity
     */
    void recordUserActivity(UUID userId, LocalDate activityDate);
    
    /**
     * Calculates and updates daily retention rate.
     * Should be called at end of day or via scheduled job.
     * 
     * @param date Date to calculate retention for
     */
    void calculateDailyRetention(LocalDate date);
    
    /**
     * Initializes analytics for a new day.
     * 
     * @param date Date to initialize
     * @return Count of total users at that date
     */
    Count initializeAnalyticsForDate(LocalDate date);
}

