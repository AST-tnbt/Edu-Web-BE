package com.se347.analysticservice.schedulers;

import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.services.admin.PlatformOverviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlatformAnalyticsScheduler {
    
    private final PlatformOverviewService platformOverviewService;
    
    @Scheduled(cron = "0 5 0 * * ?")
    public void generateDailyOverview() {
        log.info("Starting daily platform overview generation");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            platformOverviewService.generatePlatformOverview(
                Period.DAILY,
                yesterday,
                yesterday
            );
            
            log.info("Daily overview generated successfully for date: {}", yesterday);
            
        } catch (Exception e) {
            log.error("Failed to generate daily overview", e);
        }
    }
    
    @Scheduled(cron = "0 10 0 ? * MON")
    public void generateWeeklyOverview() {
        log.info("Starting weekly platform overview generation");
        
        try {
            LocalDate now = LocalDate.now();
            LocalDate startOfLastWeek = now.minusWeeks(1).minusDays(now.getDayOfWeek().getValue() - 1);
            LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);
            
            platformOverviewService.generatePlatformOverview(
                Period.WEEKLY,
                startOfLastWeek,
                endOfLastWeek
            );
            
            log.info("Weekly overview generated successfully: {} to {}", startOfLastWeek, endOfLastWeek);
            
        } catch (Exception e) {
            log.error("Failed to generate weekly overview", e);
        }
    }
    
    @Scheduled(cron = "0 15 0 1 * ?")
    public void generateMonthlyOverview() {
        log.info("Starting monthly platform overview generation");
        
        try {
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
            LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(
                firstDayOfLastMonth.lengthOfMonth()
            );
            
            platformOverviewService.generatePlatformOverview(
                Period.MONTHLY,
                firstDayOfLastMonth,
                lastDayOfLastMonth
            );
            
            log.info("Monthly overview generated successfully: {} to {}", 
                firstDayOfLastMonth, lastDayOfLastMonth);
            
        } catch (Exception e) {
            log.error("Failed to generate monthly overview", e);
        }
    }
    
    @Scheduled(cron = "0 20 0 1 1 ?")
    public void generateYearlyOverview() {
        log.info("Starting yearly platform overview generation");
        
        try {
            LocalDate now = LocalDate.now();
            int lastYear = now.getYear() - 1;
            LocalDate startOfLastYear = LocalDate.of(lastYear, 1, 1);
            LocalDate endOfLastYear = LocalDate.of(lastYear, 12, 31);
            
            platformOverviewService.generatePlatformOverview(
                Period.YEARLY,
                startOfLastYear,
                endOfLastYear
            );
            
            log.info("Yearly overview generated successfully: {} to {}", 
                startOfLastYear, endOfLastYear);
            
        } catch (Exception e) {
            log.error("Failed to generate yearly overview", e);
        }
    }
    
    @Scheduled(cron = "0 0 1 * * ?")
    public void initializeCurrentPeriods() {
        log.info("Initializing current period overviews");
        
        try {
            platformOverviewService.initializeCurrentPeriodOverview(Period.DAILY);
            platformOverviewService.initializeCurrentPeriodOverview(Period.WEEKLY);
            platformOverviewService.initializeCurrentPeriodOverview(Period.MONTHLY);
            
            log.info("Current period overviews initialized successfully");
            
        } catch (Exception e) {
            log.error("Failed to initialize current period overviews", e);
        }
    }
}

