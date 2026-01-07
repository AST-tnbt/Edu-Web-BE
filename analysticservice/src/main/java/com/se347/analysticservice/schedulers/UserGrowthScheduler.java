package com.se347.analysticservice.schedulers;

import com.se347.analysticservice.services.UserGrowthAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserGrowthScheduler {
    
    private final UserGrowthAnalyticsService userGrowthAnalyticsService;
    
    @Scheduled(cron = "0 0 23 * * ?")
    public void calculateDailyRetention() {
        log.info("Starting daily retention rate calculation");
        
        try {
            LocalDate today = LocalDate.now();
            
            userGrowthAnalyticsService.calculateDailyRetention(today);
            
            log.info("Daily retention calculated successfully for date: {}", today);
            
        } catch (Exception e) {
            log.error("Failed to calculate daily retention", e);
        }
    }
}

