package com.se347.analysticservice.domains.services.overview;

import com.se347.analysticservice.domains.services.platform.PlatformMetricsAggregationService;
import com.se347.analysticservice.domains.services.shared.PercentageCalculationHelper;
import com.se347.analysticservice.entities.admin.platform.PlatformOverview;
import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.InstructorCourseStatsRepository;
import com.se347.analysticservice.repositories.InstructorOverviewRepository;
import com.se347.analysticservice.repositories.PlatformOverviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain Service for synchronizing Overview entities with their source entities.
 * 
 * DDD PATTERN: Domain Service
 * 
 * RESPONSIBILITIES:
 * - Synchronize InstructorOverview with InstructorCourseStats
 * - Synchronize PlatformOverview with aggregated metrics
 * - Ensure data consistency across aggregates
 * 
 * USAGE:
 * This service is called when source entities change (via events or direct calls)
 * to ensure overview entities stay in sync.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OverviewSynchronizationService {
    
    private final InstructorOverviewRepository instructorOverviewRepository;
    private final InstructorCourseStatsRepository instructorCourseStatsRepository;
    private final PlatformOverviewRepository platformOverviewRepository;
    private final PlatformMetricsAggregationService platformMetricsAggregationService;
    
    /**
     * Synchronizes InstructorOverview with InstructorCourseStats.
     * 
     * BUSINESS RULE: InstructorOverview should always reflect the current state
     * of all InstructorCourseStats for that instructor.
     * 
     * @param instructorId The instructor ID to synchronize
     */
    @Transactional
    public void synchronizeInstructorOverview(UUID instructorId) {
        log.debug("Synchronizing instructor overview: instructorId={}", instructorId);
        
        // Get or create overview
        InstructorOverview overview = instructorOverviewRepository
            .findByInstructorId(instructorId)
            .orElseGet(() -> createInitialInstructorOverview(instructorId));
        
        // Aggregate from source entities
        List<InstructorCourseStats> courseStatsList = 
            instructorCourseStatsRepository.findByInstructorId(instructorId);
        
        // Calculate metrics from source data
        Count totalCourses = Count.of(courseStatsList.size());
        
        long totalStudentsValue = courseStatsList.stream()
            .mapToLong(cs -> cs.getTotalStudents().getValue())
            .sum();
        Count totalStudents = Count.of(totalStudentsValue);
        
        Money totalRevenue = courseStatsList.stream()
            .map(InstructorCourseStats::getTotalRevenue)
            .reduce(Money.zero(), Money::add);
        
        List<Percentage> completionRates = courseStatsList.stream()
            .filter(cs -> cs.getCompletionRate() != null && !cs.getCompletionRate().isZero())
            .map(InstructorCourseStats::getCompletionRate)
            .collect(Collectors.toList());
        
        Percentage averageCompletionRate = completionRates.isEmpty()
            ? Percentage.zero()
            : PercentageCalculationHelper.calculateAverageFromPercentages(completionRates);
        
        // Update overview with synchronized data
        overview.updateMetrics(totalCourses, totalStudents, totalRevenue, averageCompletionRate);
        instructorOverviewRepository.save(overview);
        
        log.info("Instructor overview synchronized: instructorId={}, totalCourses={}, totalStudents={}, totalRevenue={}, avgCompletionRate={}%",
            instructorId, totalCourses.getValue(), totalStudents.getValue(), 
            totalRevenue.getAmount(), averageCompletionRate.getValue());
    }
    
    /**
     * Synchronizes PlatformOverview for a specific period.
     * 
     * BUSINESS RULE: PlatformOverview should always reflect the current state
     * of aggregated metrics from all source entities.
     * 
     * @param period The period to synchronize
     * @param startDate Start date of the period
     * @param endDate End date of the period
     */
    @Transactional
    public void synchronizePlatformOverview(Period period, LocalDate startDate, LocalDate endDate) {
        log.debug("Synchronizing platform overview: period={}, startDate={}, endDate={}", 
            period, startDate, endDate);
        
        // Get or create overview
        Optional<PlatformOverview> existing = platformOverviewRepository
            .findByPeriodAndStartDateAndEndDate(period, startDate, endDate);
        
        PlatformOverview overview;
        if (existing.isPresent()) {
            overview = existing.get();
            log.debug("Updating existing platform overview");
        } else {
            overview = createInitialPlatformOverview(period, startDate, endDate);
            log.debug("Creating new platform overview");
        }
        
        // Aggregate from source entities
        Count totalUsers = platformMetricsAggregationService.getTotalUsersAtDate(endDate);
        Count newUsersCount = platformMetricsAggregationService.getNewUsersInPeriod(startDate, endDate);
        Count totalEnrollments = platformMetricsAggregationService.getTotalEnrollmentsUntil(endDate);
        Count newEnrollmentsCount = platformMetricsAggregationService.getNewEnrollmentsInPeriod(startDate, endDate);
        Money revenueByPeriod = platformMetricsAggregationService.getTotalRevenueInPeriod(startDate, endDate);
        Money totalRevenue = platformMetricsAggregationService.getTotalRevenueUntil(endDate);
        Percentage averageRetentionRate = platformMetricsAggregationService.getAverageRetentionInPeriod(startDate, endDate);
        
        // Update overview with synchronized data
        overview.updateMetrics(
            totalUsers, totalEnrollments, totalRevenue, revenueByPeriod,
            averageRetentionRate, newUsersCount, newEnrollmentsCount
        );
        
        platformOverviewRepository.save(overview);
        
        log.info("Platform overview synchronized: period={}, totalUsers={}, totalEnrollments={}, totalRevenue={}",
            period, totalUsers.getValue(), totalEnrollments.getValue(), totalRevenue.getAmount());
    }
    
    /**
     * Synchronizes current period PlatformOverview.
     * 
     * @param period The period to synchronize (DAILY, WEEKLY, MONTHLY)
     */
    @Transactional
    public void synchronizeCurrentPeriodOverview(Period period) {
        LocalDate[] dates = calculatePeriodDates(period);
        synchronizePlatformOverview(period, dates[0], dates[1]);
    }
    
    /**
     * Calculates start and end dates for a given period.
     * 
     * @param period The period (DAILY, WEEKLY, MONTHLY, YEARLY)
     * @return Array with [startDate, endDate]
     */
    public LocalDate[] calculatePeriodDates(Period period) {
        return calculatePeriodDatesInternal(period);
    }
    
    // ========== Private Helper Methods ==========
    
    private InstructorOverview createInitialInstructorOverview(UUID instructorId) {
        return InstructorOverview.create(
            instructorId,
            Count.zero(),
            Count.zero(),
            Money.zero(),
            Percentage.zero()
        );
    }
    
    private PlatformOverview createInitialPlatformOverview(Period period, LocalDate startDate, LocalDate endDate) {
        return PlatformOverview.create(
            period, startDate, endDate,
            Count.zero(), Count.zero(), Money.zero(), Money.zero(),
            Percentage.zero(), Count.zero(), Count.zero()
        );
    }
    
    private LocalDate[] calculatePeriodDatesInternal(Period period) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        
        switch (period) {
            case DAILY:
                startDate = now;
                endDate = now;
                break;
                
            case WEEKLY:
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                endDate = startDate.plusDays(6);
                break;
                
            case MONTHLY:
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;
                
            case YEARLY:
                startDate = now.withDayOfYear(1);
                endDate = now.withDayOfYear(now.lengthOfYear());
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
        
        return new LocalDate[]{startDate, endDate};
    }
}
