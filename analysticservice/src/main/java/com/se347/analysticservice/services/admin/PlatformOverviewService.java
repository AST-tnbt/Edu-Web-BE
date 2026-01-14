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
    
    void recordCourseCompletion(
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
    
    
    PlatformOverview initializeCurrentPeriodOverview(Period period);
}
