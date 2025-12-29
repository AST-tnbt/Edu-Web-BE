package com.se347.analysticservice.services.impls;

import com.se347.analysticservice.entities.admin.platform.PlatformOverview;
import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.PlatformOverviewRepository;
import com.se347.analysticservice.repositories.UserGrowthAnalyticsRepository;
import com.se347.analysticservice.services.PlatformOverviewService;
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
 * Implementation of PlatformOverviewService.
 * 
 * This service uses a hybrid approach:
 * 1. Real-time incremental updates for simple metrics (triggered by events)
 * 2. Batch calculation for complex aggregations (triggered by scheduled jobs)
 * 
 * Note: For MVP, we focus on batch calculation approach as it's simpler and more reliable.
 * Real-time methods are implemented as lightweight tracking that can be used for notifications.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlatformOverviewServiceImpl implements PlatformOverviewService {
    
    private final PlatformOverviewRepository platformOverviewRepository;
    private final UserGrowthAnalyticsRepository userGrowthAnalyticsRepository;
    // Note: JdbcTemplate can be added later if we need custom SQL queries
    
    // ==================== Event-Driven Updates (Real-time) ====================
    
    @Override
    public void recordCourseCreation(UUID courseId, UUID instructorId, LocalDate createdDate) {
        log.info("Recording course creation: courseId={}, instructorId={}, date={}", 
            courseId, instructorId, createdDate);
        
        // For now, we rely on batch jobs to update these metrics
        // In the future, we can implement incremental updates here
        
        log.debug("Course creation recorded. Metrics will be updated by next scheduled job.");
    }
    
    @Override
    public void recordCoursePublication(UUID courseId, UUID instructorId, LocalDate publishedDate) {
        log.info("Recording course publication: courseId={}, instructorId={}, date={}", 
            courseId, instructorId, publishedDate);
        
        // For now, we rely on batch jobs to update these metrics
        log.debug("Course publication recorded. Metrics will be updated by next scheduled job.");
    }
    
    @Override
    public void recordEnrollment(UUID enrollmentId, UUID studentId, UUID courseId,
                                 UUID instructorId, LocalDate enrolledDate) {
        log.info("Recording enrollment: enrollmentId={}, studentId={}, courseId={}, date={}", 
            enrollmentId, studentId, courseId, enrolledDate);
        
        // For now, we rely on batch jobs to update these metrics
        log.debug("Enrollment recorded. Metrics will be updated by next scheduled job.");
    }
    
    @Override
    public void recordPayment(UUID paymentId, UUID courseId, UUID instructorId,
                             BigDecimal amount, BigDecimal platformFee,
                             BigDecimal instructorEarning, LocalDate paymentDate) {
        log.info("Recording payment: paymentId={}, amount={}, platformFee={}, date={}", 
            paymentId, amount, platformFee, paymentDate);
        
        // For now, we rely on batch jobs to update these metrics
        log.debug("Payment recorded. Metrics will be updated by next scheduled job.");
    }
    
    @Override
    public void recordCourseCompletion(UUID studentId, UUID courseId, UUID instructorId,
                                      UUID enrollmentId, LocalDate completedDate) {
        log.info("Recording course completion: studentId={}, courseId={}, date={}", 
            studentId, courseId, completedDate);
        
        // For now, we rely on batch jobs to update these metrics
        log.debug("Course completion recorded. Metrics will be updated by next scheduled job.");
    }
    
    @Override
    public void recordProgressUpdate(UUID studentId, UUID courseId, UUID instructorId,
                                    Double completionRate, LocalDate updateDate) {
        log.debug("Recording progress update: studentId={}, courseId={}, rate={}%", 
            studentId, courseId, completionRate);
        
        // For now, we rely on batch jobs to update these metrics
    }
    
    // ==================== Batch Operations (Scheduled) ====================
    
    @Override
    public PlatformOverview generatePlatformOverview(Period period, LocalDate startDate, LocalDate endDate) {
        log.info("Generating platform overview: period={}, startDate={}, endDate={}", 
            period, startDate, endDate);
        
        try {
            // Check if overview already exists
            Optional<PlatformOverview> existingOverview = platformOverviewRepository
                .findByPeriodAndStartDateAndEndDate(period, startDate, endDate);
            
            if (existingOverview.isPresent()) {
                log.info("Overview already exists. Updating metrics...");
                PlatformOverview overview = existingOverview.get();
                updateOverviewMetrics(overview, startDate, endDate);
                return platformOverviewRepository.save(overview);
            }
            
            // Create new overview
            PlatformOverview overview = createNewOverview(period, startDate, endDate);
            PlatformOverview saved = platformOverviewRepository.save(overview);
            
            log.info("Platform overview generated successfully: id={}", saved.getPlatformOverviewId());
            return saved;
            
        } catch (Exception e) {
            log.error("Failed to generate platform overview", e);
            throw new RuntimeException("Failed to generate platform overview", e);
        }
    }
    
    @Override
    public Optional<PlatformOverview> getLatestOverview(Period period) {
        return platformOverviewRepository.findLatestByPeriod(period);
    }
    
    @Override
    public Optional<PlatformOverview> getOverviewForPeriod(Period period, LocalDate startDate, LocalDate endDate) {
        return platformOverviewRepository.findByPeriodAndStartDateAndEndDate(period, startDate, endDate);
    }
    
    @Override
    public void recalculateOverview(UUID overviewId) {
        log.info("Recalculating overview: id={}", overviewId);
        
        PlatformOverview overview = platformOverviewRepository.findById(overviewId)
            .orElseThrow(() -> new IllegalArgumentException("PlatformOverview not found: " + overviewId));
        
        updateOverviewMetrics(overview, overview.getStartDate(), overview.getEndDate());
        platformOverviewRepository.save(overview);
        
        log.info("Overview recalculated successfully: id={}", overviewId);
    }
    
    @Override
    public PlatformOverview initializeCurrentPeriodOverview(Period period) {
        LocalDate[] dates = calculatePeriodDates(period);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];
        
        Optional<PlatformOverview> existing = platformOverviewRepository
            .findByPeriodAndStartDateAndEndDate(period, startDate, endDate);
        
        if (existing.isPresent()) {
            log.info("Current period overview already exists: period={}", period);
            return existing.get();
        }
        
        log.info("Initializing new period overview: period={}, startDate={}, endDate={}", 
            period, startDate, endDate);
        
        return generatePlatformOverview(period, startDate, endDate);
    }
    
    // ==================== Private Helper Methods ====================
    
    /**
     * Creates a new PlatformOverview by aggregating data from various sources.
     */
    private PlatformOverview createNewOverview(Period period, LocalDate startDate, LocalDate endDate) {
        
        // 1. Aggregate user metrics from UserGrowthAnalytics
        Count totalUsers = calculateTotalUsers(endDate);
        Count newUsersCount = calculateNewUsersInPeriod(startDate, endDate);
        
        // 2. Aggregate course metrics (from course service data)
        Count totalActiveCourses = calculateTotalActiveCourses();
        Count newCoursesCount = calculateNewCoursesInPeriod(startDate, endDate);
        
        // 3. Aggregate enrollment metrics
        Count totalEnrollments = calculateTotalEnrollments();
        Count newEnrollmentsCount = calculateNewEnrollmentsInPeriod(startDate, endDate);
        
        // 4. Aggregate revenue metrics
        Money totalRevenue = calculateTotalRevenue();
        
        // 5. Calculate average completion rate
        Percentage averageCompletionRate = calculateAverageCompletionRate();
        
        // 6. Calculate growth rates (compare with previous period)
        Optional<PlatformOverview> previousPeriod = platformOverviewRepository
            .findPreviousPeriod(period, endDate);
        
        Percentage userGrowthRate = calculateGrowthRate(
            previousPeriod.map(po -> po.getTotalUsers()).orElse(Count.zero()),
            totalUsers
        );
        
        Percentage revenueGrowthRate = calculateRevenueGrowthRate(
            previousPeriod.map(po -> po.getTotalRevenue()).orElse(Money.zero()),
            totalRevenue
        );
        
        Percentage enrollmentGrowthRate = calculateGrowthRate(
            previousPeriod.map(po -> po.getTotalEnrollments()).orElse(Count.zero()),
            totalEnrollments
        );
        
        // Create aggregate
        return PlatformOverview.create(
            period,
            startDate,
            endDate,
            totalUsers,
            totalActiveCourses,
            Count.zero(), // totalInstructors - will be calculated separately
            totalEnrollments,
            totalRevenue,
            averageCompletionRate,
            newUsersCount,
            newCoursesCount,
            newEnrollmentsCount,
            userGrowthRate,
            revenueGrowthRate,
            enrollmentGrowthRate
        );
    }
    
    /**
     * Updates metrics for an existing overview.
     */
    private void updateOverviewMetrics(PlatformOverview overview, LocalDate startDate, LocalDate endDate) {
        
        Count totalUsers = calculateTotalUsers(endDate);
        Count newUsersCount = calculateNewUsersInPeriod(startDate, endDate);
        Count totalActiveCourses = calculateTotalActiveCourses();
        Count newCoursesCount = calculateNewCoursesInPeriod(startDate, endDate);
        Count totalEnrollments = calculateTotalEnrollments();
        Count newEnrollmentsCount = calculateNewEnrollmentsInPeriod(startDate, endDate);
        Money totalRevenue = calculateTotalRevenue();
        Percentage averageCompletionRate = calculateAverageCompletionRate();
        
        // Calculate growth rates
        Optional<PlatformOverview> previousPeriod = platformOverviewRepository
            .findPreviousPeriod(overview.getPeriod(), endDate);
        
        Percentage userGrowthRate = calculateGrowthRate(
            previousPeriod.map(po -> po.getTotalUsers()).orElse(Count.zero()),
            totalUsers
        );
        
        Percentage revenueGrowthRate = calculateRevenueGrowthRate(
            previousPeriod.map(po -> po.getTotalRevenue()).orElse(Money.zero()),
            totalRevenue
        );
        
        Percentage enrollmentGrowthRate = calculateGrowthRate(
            previousPeriod.map(po -> po.getTotalEnrollments()).orElse(Count.zero()),
            totalEnrollments
        );
        
        // Update aggregate
        overview.updateMetrics(
            totalUsers,
            totalActiveCourses,
            Count.zero(), // totalInstructors
            totalEnrollments,
            totalRevenue,
            averageCompletionRate,
            newUsersCount,
            newCoursesCount,
            newEnrollmentsCount,
            userGrowthRate,
            revenueGrowthRate,
            enrollmentGrowthRate
        );
    }
    
    // ==================== Metric Calculation Methods ====================
    
    /**
     * Calculates total users at a given date.
     * Queries the most recent UserGrowthAnalytics record.
     */
    private Count calculateTotalUsers(LocalDate date) {
        Optional<UserGrowthAnalytics> analytics = userGrowthAnalyticsRepository.findByDate(date);
        
        if (analytics.isPresent()) {
            return analytics.get().getTotalUsers();
        }
        
        // Fallback: Get most recent analytics
        Optional<UserGrowthAnalytics> mostRecent = userGrowthAnalyticsRepository.findMostRecent();
        return mostRecent.map(UserGrowthAnalytics::getTotalUsers).orElse(Count.zero());
    }
    
    /**
     * Calculates new users registered in the period.
     */
    private Count calculateNewUsersInPeriod(LocalDate startDate, LocalDate endDate) {
        List<UserGrowthAnalytics> analytics = userGrowthAnalyticsRepository
            .findByDateBetween(startDate, endDate);
        
        long totalNewUsers = analytics.stream()
            .mapToLong(a -> a.getNewUsersCount().getValue())
            .sum();
        
        return Count.of(totalNewUsers);
    }
    
    /**
     * Calculates total active courses.
     * Note: This requires querying the Course Service database or having a local cache.
     * For now, returning zero as placeholder.
     */
    private Count calculateTotalActiveCourses() {
        // TODO: Implement query to course service or local analytics table
        // SELECT COUNT(*) FROM courses WHERE status = 'PUBLISHED'
        log.warn("calculateTotalActiveCourses not fully implemented - returning zero");
        return Count.zero();
    }
    
    /**
     * Calculates new courses created in the period.
     */
    private Count calculateNewCoursesInPeriod(LocalDate startDate, LocalDate endDate) {
        // TODO: Implement query to course analytics table
        // SELECT COUNT(*) FROM course_analytics WHERE created_date BETWEEN startDate AND endDate
        log.warn("calculateNewCoursesInPeriod not fully implemented - returning zero");
        return Count.zero();
    }
    
    /**
     * Calculates total enrollments.
     */
    private Count calculateTotalEnrollments() {
        // TODO: Implement query to enrollment analytics table
        log.warn("calculateTotalEnrollments not fully implemented - returning zero");
        return Count.zero();
    }
    
    /**
     * Calculates new enrollments in the period.
     */
    private Count calculateNewEnrollmentsInPeriod(LocalDate startDate, LocalDate endDate) {
        // TODO: Implement query to enrollment analytics table
        log.warn("calculateNewEnrollmentsInPeriod not fully implemented - returning zero");
        return Count.zero();
    }
    
    /**
     * Calculates total revenue.
     */
    private Money calculateTotalRevenue() {
        // TODO: Implement query to payment analytics table
        log.warn("calculateTotalRevenue not fully implemented - returning zero");
        return Money.zero();
    }
    
    /**
     * Calculates average completion rate across all enrollments.
     */
    private Percentage calculateAverageCompletionRate() {
        // TODO: Implement query to progress analytics table
        log.warn("calculateAverageCompletionRate not fully implemented - returning zero");
        return Percentage.zero();
    }
    
    /**
     * Calculates growth rate between two counts.
     */
    private Percentage calculateGrowthRate(Count previous, Count current) {
        if (previous.isZero()) {
            return current.isZero() ? Percentage.zero() : Percentage.of(100.0);
        }
        
        double growthRate = ((current.getValue() - previous.getValue()) * 100.0) / previous.getValue();
        return Percentage.of(growthRate);
    }
    
    /**
     * Calculates revenue growth rate.
     */
    private Percentage calculateRevenueGrowthRate(Money previous, Money current) {
        if (previous.isZero()) {
            return current.isZero() ? Percentage.zero() : Percentage.of(100.0);
        }
        
        double previousAmount = previous.getAmount().doubleValue();
        double currentAmount = current.getAmount().doubleValue();
        double growthRate = ((currentAmount - previousAmount) * 100.0) / previousAmount;
        
        return Percentage.of(growthRate);
    }
    
    /**
     * Calculates start and end dates for a given period.
     * Returns array: [startDate, endDate]
     */
    private LocalDate[] calculatePeriodDates(Period period) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        
        switch (period) {
            case DAILY:
                startDate = now;
                endDate = now;
                break;
                
            case WEEKLY:
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1); // Start of week (Monday)
                endDate = startDate.plusDays(6); // End of week (Sunday)
                break;
                
            case MONTHLY:
                startDate = now.withDayOfMonth(1); // First day of month
                endDate = now.withDayOfMonth(now.lengthOfMonth()); // Last day of month
                break;
                
            case YEARLY:
                startDate = now.withDayOfYear(1); // First day of year
                endDate = now.withDayOfYear(now.lengthOfYear()); // Last day of year
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
        
        return new LocalDate[]{startDate, endDate};
    }
}

