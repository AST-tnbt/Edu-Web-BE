package com.se347.analysticservice.services.admin;

import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGrowthAnalyticsService {
    
    void recordUserRegistration(UUID userId, LocalDate registrationDate);
    
    void recordUserActivity(UUID userId, LocalDate activityDate);
    
    void calculateDailyRetention(LocalDate date);
    
    Count initializeAnalyticsForDate(LocalDate date);
    
    Optional<UserGrowthAnalytics> getAnalyticsForDate(LocalDate date);
    
    List<UserGrowthAnalytics> getAnalyticsForPeriod(LocalDate startDate, LocalDate endDate);
    
    Percentage getAverageRetentionRate(LocalDate startDate, LocalDate endDate);
    
    Count getTotalActiveUsers(LocalDate date);
    
    UserGrowthAnalytics getMostRecentAnalytics();
}
