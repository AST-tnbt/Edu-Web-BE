package com.se347.analysticservice.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.se347.analysticservice.services.admin.RevenueAnalyticsService;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class RevenueAnalyticsScheduler {
    
    private final RevenueAnalyticsService revenueAnalyticsService;
    
    @Scheduled(cron = "0 30 0 * * ?")
    public void generateDailyRevenue() {
        log.info("Starting daily revenue generation");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            revenueAnalyticsService.generateDailyRevenue(yesterday);
            
            log.info("Daily revenue generated successfully for date: {}", yesterday);
            
        } catch (Exception e) {
            log.error("Failed to generate daily revenue", e);
        }
    }
}

