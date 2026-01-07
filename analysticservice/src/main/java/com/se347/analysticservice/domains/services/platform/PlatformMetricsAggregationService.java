package com.se347.analysticservice.domains.services.platform;

import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.UserGrowthAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformMetricsAggregationService {
    
    private final UserGrowthAnalyticsRepository userGrowthAnalyticsRepository;
    
    public Count getTotalUsersAtDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return userGrowthAnalyticsRepository.findByDate(date)
            .map(UserGrowthAnalytics::getTotalUsers)
            .orElseGet(() -> userGrowthAnalyticsRepository.findMostRecent()
                .map(UserGrowthAnalytics::getTotalUsers)
                .orElse(Count.zero()));
    }
    
    public Count getNewUsersInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<UserGrowthAnalytics> analytics = userGrowthAnalyticsRepository
            .findByDateBetween(startDate, endDate);
        
        long total = analytics.stream()
            .mapToLong(a -> a.getNewUsersCount().getValue())
            .sum();
        
        return Count.of(total);
    }
    
    public Count getAverageActiveUsersInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<UserGrowthAnalytics> analytics = userGrowthAnalyticsRepository
            .findByDateBetween(startDate, endDate);
        
        if (analytics.isEmpty()) {
            return Count.zero();
        }
        
        long totalActive = analytics.stream()
            .mapToLong(a -> a.getActiveUsersCount().getValue())
            .sum();
        
        long average = totalActive / analytics.size();
        return Count.of(average);
    }
    
    public Percentage getAverageRetentionInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<UserGrowthAnalytics> analytics = userGrowthAnalyticsRepository
            .findByDateBetween(startDate, endDate);
        
        if (analytics.isEmpty()) {
            return Percentage.zero();
        }
        
        double avgRetention = analytics.stream()
            .map(UserGrowthAnalytics::getRetentionRate)
            .filter(rate -> rate != null && !rate.isZero())
            .mapToDouble(Percentage::getValue)
            .average()
            .orElse(0.0);
        
        return Percentage.of(avgRetention);
    }
    
    public Count getMostRecentTotalUsers() {
        return userGrowthAnalyticsRepository.findMostRecent()
            .map(UserGrowthAnalytics::getTotalUsers)
            .orElse(Count.zero());
    }
    
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }
}
