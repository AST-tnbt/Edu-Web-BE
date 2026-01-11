package com.se347.analysticservice.schedulers;

import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.services.admin.InstructorRevenueGenerationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class InstructorAnalyticsScheduler {
    
    private final InstructorRevenueGenerationService revenueGenerationService;
    
    @Scheduled(cron = "0 40 0 * * ?")
    public void generateDailyInstructorRevenue() {
        log.info("Starting daily instructor revenue generation");
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            List<UUID> activeInstructors = getActiveInstructors();
            
            for (UUID instructorId : activeInstructors) {
                try {
                    revenueGenerationService.generateInstructorRevenue(
                        instructorId,
                        Period.DAILY,
                        yesterday,
                        yesterday
                    );
                } catch (Exception e) {
                    log.error("Failed to generate revenue for instructor: {}", instructorId, e);
                }
            }
            
            log.info("Daily instructor revenue generated for {} instructors", activeInstructors.size());
            
        } catch (Exception e) {
            log.error("Failed to generate daily instructor revenue", e);
        }
    }
    
    @Scheduled(cron = "0 45 0 ? * MON")
    public void generateWeeklyInstructorRevenue() {
        log.info("Starting weekly instructor revenue generation");
        
        try {
            LocalDate now = LocalDate.now();
            LocalDate startOfLastWeek = now.minusWeeks(1).minusDays(now.getDayOfWeek().getValue() - 1);
            LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);
            
            List<UUID> activeInstructors = getActiveInstructors();
            
            for (UUID instructorId : activeInstructors) {
                try {
                    revenueGenerationService.generateInstructorRevenue(
                        instructorId,
                        Period.WEEKLY,
                        startOfLastWeek,
                        endOfLastWeek
                    );
                } catch (Exception e) {
                    log.error("Failed to generate weekly revenue for instructor: {}", instructorId, e);
                }
            }
            
            log.info("Weekly instructor revenue generated for {} instructors", activeInstructors.size());
            
        } catch (Exception e) {
            log.error("Failed to generate weekly instructor revenue", e);
        }
    }
    
    @Scheduled(cron = "0 50 0 1 * ?")
    public void generateMonthlyInstructorRevenue() {
        log.info("Starting monthly instructor revenue generation");
        
        try {
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
            LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(
                firstDayOfLastMonth.lengthOfMonth()
            );
            
            List<UUID> activeInstructors = getActiveInstructors();
            
            for (UUID instructorId : activeInstructors) {
                try {
                    revenueGenerationService.generateInstructorRevenue(
                        instructorId,
                        Period.MONTHLY,
                        firstDayOfLastMonth,
                        lastDayOfLastMonth
                    );
                } catch (Exception e) {
                    log.error("Failed to generate monthly revenue for instructor: {}", instructorId, e);
                }
            }
            
            log.info("Monthly instructor revenue generated for {} instructors", activeInstructors.size());
            
        } catch (Exception e) {
            log.error("Failed to generate monthly instructor revenue", e);
        }
    }
    
    private List<UUID> getActiveInstructors() {
        return List.of();
    }
}

