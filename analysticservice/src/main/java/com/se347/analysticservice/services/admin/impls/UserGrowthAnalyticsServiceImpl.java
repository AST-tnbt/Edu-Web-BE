package com.se347.analysticservice.services.admin.impls;

import com.se347.analysticservice.domains.services.platform.PlatformMetricsAggregationService;
import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.UserGrowthAnalyticsRepository;
import com.se347.analysticservice.services.admin.UserGrowthAnalyticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserGrowthAnalyticsServiceImpl implements UserGrowthAnalyticsService {
    
    private final UserGrowthAnalyticsRepository repository;
    private final PlatformMetricsAggregationService aggregationService;
    
    @Override
    @Transactional
    public void recordUserRegistration(UUID userId, LocalDate registrationDate) {
        UserGrowthAnalytics analytics = repository.findByDate(registrationDate)
            .orElseGet(() -> createInitialAnalytics(registrationDate));
        
        analytics.recordNewUser();
        
        repository.save(analytics);
    }
    
    @Override
    @Transactional
    public void recordUserActivity(UUID userId, LocalDate activityDate) {
        UserGrowthAnalytics analytics = repository.findByDate(activityDate)
            .orElseGet(() -> createInitialAnalytics(activityDate));
        
        analytics.recordActiveUser();
        
        repository.save(analytics);
    }
    
    @Override
    @Transactional
    public void calculateDailyRetention(LocalDate date) {
        LocalDate yesterday = date.minusDays(1);
        
        UserGrowthAnalytics yesterdayAnalytics = repository.findByDate(yesterday).orElse(null);
        if (yesterdayAnalytics == null) return;
        
        UserGrowthAnalytics todayAnalytics = repository.findByDate(date).orElse(null);
        if (todayAnalytics == null) return;
        
        Percentage retention = todayAnalytics.calculateRetentionRate(
            yesterdayAnalytics.getActiveUsersCount()
        );
        
        todayAnalytics.updateRetentionRate(retention);
        repository.save(todayAnalytics);
    }
    
    @Override
    @Transactional
    public Count initializeAnalyticsForDate(LocalDate date) {
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
    
    private UserGrowthAnalytics createInitialAnalytics(LocalDate date) {
        Count totalUsers = initializeAnalyticsForDate(date);
        
        return UserGrowthAnalytics.create(
            date,
            Count.zero(),
            Count.zero(),
            totalUsers,
            Percentage.zero()
        );
    }
}

