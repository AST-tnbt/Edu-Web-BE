package com.se347.analysticservice.services;

import com.se347.analysticservice.entities.admin.platform.PlatformOverview;
import com.se347.analysticservice.enums.Period;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for Platform Overview Analytics.
 * Orchestrates business workflows for tracking platform-wide metrics.
 * 
 * This service aggregates data from multiple sources:
 * - User registrations (from UserGrowthAnalytics)
 * - Course creations and publications
 * - Enrollments
 * - Revenue from payments
 * - Course completion rates
 */
public interface PlatformOverviewService {
    
    // ==================== Event-Driven Updates (Real-time) ====================
    
    /**
     * Records a new course creation.
     * Updates course count metrics.
     */
    void recordCourseCreation(UUID courseId, UUID instructorId, LocalDate createdDate);
    
    /**
     * Records a course publication (when course becomes active).
     * Updates active course count.
     */
    void recordCoursePublication(UUID courseId, UUID instructorId, LocalDate publishedDate);
    
    /**
     * Records a new enrollment.
     * Updates enrollment count metrics.
     */
    void recordEnrollment(
        UUID enrollmentId, 
        UUID studentId, 
        UUID courseId,
        UUID instructorId,
        LocalDate enrolledDate
    );
    
    /**
     * Records a completed payment.
     * Updates revenue metrics for platform.
     */
    void recordPayment(
        UUID paymentId,
        UUID courseId,
        UUID instructorId,
        BigDecimal amount,
        BigDecimal platformFee,
        BigDecimal instructorEarning,
        LocalDate paymentDate
    );
    
    /**
     * Records a course completion.
     * Updates completion rate metrics.
     */
    void recordCourseCompletion(
        UUID studentId,
        UUID courseId,
        UUID instructorId,
        UUID enrollmentId,
        LocalDate completedDate
    );
    
    /**
     * Records a progress update.
     * Used for calculating average completion rate.
     */
    void recordProgressUpdate(
        UUID studentId,
        UUID courseId,
        UUID instructorId,
        Double completionRate,
        LocalDate updateDate
    );
    
    // ==================== Batch Operations (Scheduled) ====================
    
    /**
     * Generates or updates platform overview for a specific period.
     * This is typically called by a scheduled job.
     * 
     * Aggregates all metrics:
     * - Queries database for current totals
     * - Calculates growth rates compared to previous period
     * - Updates or creates PlatformOverview aggregate
     * 
     * @param period The time period (DAILY, WEEKLY, MONTHLY, YEARLY)
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return The created or updated PlatformOverview
     */
    PlatformOverview generatePlatformOverview(
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    /**
     * Retrieves the latest platform overview for a given period.
     * 
     * @param period The time period
     * @return Optional containing the latest overview, or empty if none exists
     */
    Optional<PlatformOverview> getLatestOverview(Period period);
    
    /**
     * Retrieves platform overview for a specific date range.
     * 
     * @param period The time period
     * @param startDate Start date
     * @param endDate End date
     * @return Optional containing the overview, or empty if none exists
     */
    Optional<PlatformOverview> getOverviewForPeriod(
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    /**
     * Recalculates and updates metrics for an existing platform overview.
     * Useful for fixing data inconsistencies or updating historical data.
     * 
     * @param overviewId ID of the overview to recalculate
     */
    void recalculateOverview(UUID overviewId);
    
    /**
     * Initializes platform overview for the current period if it doesn't exist.
     * Called at the beginning of each period (day, week, month).
     * 
     * @param period The time period
     * @return The initialized or existing overview
     */
    PlatformOverview initializeCurrentPeriodOverview(Period period);
}

