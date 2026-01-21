package com.se347.analysticservice.services.admin;

import com.se347.analysticservice.entities.admin.platform.PlatformOverview;
import com.se347.analysticservice.enums.Period;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformOverviewService {
    
    void recordCourseCreation(UUID courseId, UUID instructorId, LocalDate createdDate);
    
    void recordCoursePublication(UUID courseId, UUID instructorId, LocalDate publishedDate);
    
    void recordEnrollment(
        UUID enrollmentId, 
        UUID studentId, 
        UUID courseId,
        UUID instructorId,
        LocalDate enrolledDate
    );
    
    void recordPayment(
        UUID paymentId,
        UUID courseId,
        BigDecimal amount,
        LocalDate paymentDate
    );
    
    void recordEnrollmentCompletion(
        UUID studentId,
        UUID courseId,
        UUID instructorId,
        UUID enrollmentId,
        LocalDate completedDate
    );
    
    void recordProgressUpdate(
        UUID studentId,
        UUID courseId,
        UUID instructorId,
        Double completionRate,
        LocalDate updateDate
    );
    
    PlatformOverview generatePlatformOverview(
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    PlatformOverview getLatestOverview(Period period);

    Optional<PlatformOverview> getOverviewForPeriod(
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    List<PlatformOverview> getOverviewHistory(Period period, int limit);
    
    /**
     * Gets chart data for the most recent period.
     * - For MONTHLY: returns 30 DAILY overviews of the most recent month
     * - For WEEKLY: returns 7 DAILY overviews of the most recent week
     * - For YEARLY: returns 12 MONTHLY overviews of the most recent year
     * - For DAILY: returns the most recent DAILY overview
     * 
     * @param period The period type for the chart
     * @return List of PlatformOverview records suitable for charting
     */
    List<PlatformOverview> getChartData(Period period);
    
    PlatformOverview initializeCurrentPeriodOverview(Period period);
    
    void recalculateCurrentPeriodOverview(Period period);
}
