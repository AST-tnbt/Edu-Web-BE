package com.se347.analysticservice.services.admin.impls;

import com.se347.analysticservice.domains.services.overview.OverviewSynchronizationService;
import com.se347.analysticservice.entities.admin.platform.PlatformOverview;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.PlatformOverviewRepository;
import com.se347.analysticservice.services.admin.PlatformOverviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for PlatformOverview.
 * 
 * DDD PATTERN: Application Service
 * 
 * RESPONSIBILITIES:
 * - Orchestrate use cases (generate overview, initialize period, etc.)
 * - Handle transaction boundaries
 * - Coordinate between repositories and domain services
 * 
 * BUSINESS LOGIC:
 * - Delegated to OverviewSynchronizationService (domain service)
 * - Aggregation logic delegated to PlatformMetricsAggregationService (domain service)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlatformOverviewServiceImpl implements PlatformOverviewService {
    
    private final PlatformOverviewRepository repository;
    private final OverviewSynchronizationService overviewSynchronizationService;
    
    @Override
    @Transactional
    public void recordCourseCreation(UUID courseId, UUID instructorId, LocalDate createdDate) {
        log.info("Course created recorded: courseId={}, instructorId={}, date={}", 
            courseId, instructorId, createdDate);
    }
    
    @Override
    @Transactional
    public void recordCoursePublication(UUID courseId, UUID instructorId, LocalDate publishedDate) {
        log.info("Course published recorded: courseId={}, instructorId={}, date={}", 
            courseId, instructorId, publishedDate);
    }
    
    @Override
    @Transactional
    public void recordEnrollment(UUID enrollmentId, UUID studentId, UUID courseId, 
                                 UUID instructorId, LocalDate enrolledDate) {
        log.info("Enrollment recorded: enrollmentId={}, studentId={}, courseId={}, date={}", 
            enrollmentId, studentId, courseId, enrolledDate);
    }
    
    @Override
    @Transactional
    public void recordPayment(UUID paymentId, UUID courseId,
                             BigDecimal amount, LocalDate paymentDate) {
        log.info("Payment recorded: paymentId={}, amount={}, date={}", 
            paymentId, amount, paymentDate);
    }
    
    @Override
    @Transactional
    public void recordEnrollmentCompletion(UUID studentId, UUID courseId, UUID instructorId, 
                                       UUID enrollmentId, LocalDate completedDate) {
        log.info("Enrollment completion recorded: studentId={}, courseId={}, date={}", 
            studentId, courseId, completedDate);
    }
    
    @Override
    @Transactional
    public void recordProgressUpdate(UUID studentId, UUID courseId, UUID instructorId, 
                                     Double completionRate, LocalDate updateDate) {
        log.info("Progress update recorded: studentId={}, courseId={}, rate={}%", 
            studentId, courseId, completionRate);
    }
    
    @Override
    @Transactional
    public PlatformOverview generatePlatformOverview(Period period, LocalDate startDate, LocalDate endDate) {
        log.info("Generating platform overview: period={}, startDate={}, endDate={}", 
            period, startDate, endDate);
        
        // Delegate to domain service (contains business rules for synchronization)
        overviewSynchronizationService.synchronizePlatformOverview(period, startDate, endDate);
        
        // Return the synchronized overview
        return repository
            .findByPeriodAndStartDateAndEndDate(period, startDate, endDate)
            .orElseThrow(() -> new IllegalStateException(
                "Failed to generate platform overview for period=" + period + 
                ", startDate=" + startDate + ", endDate=" + endDate
            ));
    }

    @Override
    @Transactional
    public PlatformOverview getLatestOverview(Period period) {
        return repository.findLatestByPeriod(period).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PlatformOverview> getOverviewForPeriod(Period period, LocalDate startDate, LocalDate endDate) {
        return repository.findByPeriodAndStartDateAndEndDate(period, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PlatformOverview> getOverviewHistory(Period period, int limit) {
        return repository.findAllByPeriodOrderByEndDateDesc(period)
            .stream()
            .limit(limit)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PlatformOverview> getChartData(Period period) {
        log.info("Getting chart data for period: {}", period);
        
        LocalDate today = LocalDate.now();
        
        switch (period) {
            case MONTHLY:
                // For monthly chart: get 30 DAILY overviews of the most recent month
                LocalDate monthStart = today.withDayOfMonth(1);
                LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
                log.info("Monthly chart: fetching DAILY overviews from {} to {}", monthStart, monthEnd);
                return repository.findByPeriodAndDateRange(Period.DAILY, monthStart, monthEnd);
                
            case WEEKLY:
                // For weekly chart: get 7 DAILY overviews of the most recent week
                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                log.info("Weekly chart: fetching DAILY overviews from {} to {}", weekStart, weekEnd);
                return repository.findByPeriodAndDateRange(Period.DAILY, weekStart, weekEnd);
                
            case YEARLY:
                // For yearly chart: get 12 MONTHLY overviews of the most recent year
                LocalDate yearStart = today.withDayOfYear(1);
                LocalDate yearEnd = today.withDayOfYear(today.lengthOfYear());
                log.info("Yearly chart: fetching MONTHLY overviews from {} to {}", yearStart, yearEnd);
                return repository.findByPeriodAndDateRange(Period.MONTHLY, yearStart, yearEnd);
                
            case DAILY:
                // For daily chart: get the most recent DAILY overview
                PlatformOverview latest = repository.findLatestByPeriod(Period.DAILY).orElse(null);
                return latest != null ? List.of(latest) : List.of();
                
            default:
                log.warn("Unsupported period for chart data: {}", period);
                return List.of();
        }
    }
    
    @Override
    @Transactional
    public PlatformOverview initializeCurrentPeriodOverview(Period period) {
        log.info("Initializing current period overview: period={}", period);
        
        // Delegate to domain service (contains business rules for period calculation and synchronization)
        overviewSynchronizationService.synchronizeCurrentPeriodOverview(period);
        
        // Get the dates that were calculated
        LocalDate[] dates = overviewSynchronizationService.calculatePeriodDates(period);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];
        
        // Return the synchronized overview
        return repository
            .findByPeriodAndStartDateAndEndDate(period, startDate, endDate)
            .orElseThrow(() -> new IllegalStateException(
                "Failed to initialize platform overview for period=" + period
            ));
    }
    
    @Override
    @Transactional
    public void recalculateCurrentPeriodOverview(Period period) {
        log.info("Recalculating current period overview: period={}", period);
        
        // Delegate to domain service for synchronization
        // DDD PATTERN: Domain Service handles cross-aggregate synchronization logic
        overviewSynchronizationService.synchronizeCurrentPeriodOverview(period);
    }
}

