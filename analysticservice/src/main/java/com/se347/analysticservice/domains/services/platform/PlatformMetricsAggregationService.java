package com.se347.analysticservice.domains.services.platform;

import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.entities.admin.revenue.DailyRevenue;
import com.se347.analysticservice.repositories.DailyRevenueRepository;
import com.se347.analysticservice.repositories.InstructorDailyStatsRepository;
import com.se347.analysticservice.repositories.InstructorOverviewRepository;
import com.se347.analysticservice.repositories.UserGrowthAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformMetricsAggregationService {
    
    private final UserGrowthAnalyticsRepository userGrowthAnalyticsRepository;
    private final DailyRevenueRepository dailyRevenueRepository;
    private final InstructorDailyStatsRepository instructorDailyStatsRepository;
    private final InstructorOverviewRepository instructorOverviewRepository;
    
    public Count getTotalUsersAtDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        return userGrowthAnalyticsRepository.findByDate(date)
            .map(UserGrowthAnalytics::getTotalUsers)
            .orElseGet(() -> userGrowthAnalyticsRepository.findMostRecent()
                .map(UserGrowthAnalytics::getTotalUsers)
                .orElse(Count.zero()));
    }
    
    public Count getNewUsersInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<UserGrowthAnalytics> analytics = userGrowthAnalyticsRepository
            .findByDateBetween(startDate, endDate);
        
        long total = analytics.stream()
            .mapToLong(a -> a.getNewUsersCount().getValue())
            .sum();
        
        return Count.of(total);
    }

    public Count getTotalActiveUsersInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<UserGrowthAnalytics> analytics = userGrowthAnalyticsRepository
            .findByDateBetween(startDate, endDate);
        
        long total = analytics.stream()
            .mapToLong(a -> a.getActiveUsersCount().getValue())
            .sum();
        
        return Count.of(total);
    }

    public Count getTotalEnrollmentsInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);

        List<InstructorDailyStats> stats = instructorDailyStatsRepository.findByDateBetween(startDate, endDate);
        long total = stats.stream()
            .mapToLong(s -> s.getNewEnrollments().getValue())
            .sum();
        return Count.of(total);
    }

    public Count getNewEnrollmentsInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        List<InstructorDailyStats> stats = instructorDailyStatsRepository.findByDateBetween(startDate, endDate);
        long total = stats.stream()
            .mapToLong(s -> s.getNewEnrollments().getValue())
            .sum();
        return Count.of(total);
    }

    /**
     * Total enrollments accumulated until (and including) endDate.
     * If endDate is null, throw; if no data, returns zero.
     */
    public Count getTotalEnrollmentsUntil(LocalDate endDate) {
        if (endDate == null) throw new IllegalArgumentException("Date cannot be null");
        List<InstructorDailyStats> stats = instructorDailyStatsRepository.findByDateBetween(LocalDate.MIN, endDate);
        long total = stats.stream()
            .mapToLong(s -> s.getNewEnrollments().getValue())
            .sum();
        return Count.of(total);
    }

    public Count getTotalActiveCoursesInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        // Lấy tổng courses hiện tại từ InstructorOverview (projection mức instructor)
        List<InstructorOverview> overviews = instructorOverviewRepository.findAll();
        long total = overviews.stream()
            .mapToLong(o -> o.getTotalCourses().getValue())
            .sum();
        return Count.of(total);
    }

    public Money getTotalRevenueInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        List<DailyRevenue> revenues = dailyRevenueRepository.findByDateBetween(startDate, endDate);
        Money total = Money.zero();
        for (DailyRevenue dr : revenues) {
            total = total.add(dr.getTotalRevenue());
        }
        return total;
    }

    /**
     * Total revenue accumulated until (and including) endDate.
     */
    public Money getTotalRevenueUntil(LocalDate endDate) {
        if (endDate == null) throw new IllegalArgumentException("Date cannot be null");
        List<DailyRevenue> revenues = dailyRevenueRepository.findByDateBetween(LocalDate.MIN, endDate);
        Money total = Money.zero();
        for (DailyRevenue dr : revenues) {
            total = total.add(dr.getTotalRevenue());
        }
        return total;
    }
    
    public Percentage getAverageRetentionInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<UserGrowthAnalytics> analytics = userGrowthAnalyticsRepository
            .findByDateBetween(startDate, endDate);
        
        if (analytics.isEmpty()) {
            return Percentage.zero();
        }
        
        double avgRetention = analytics.stream()
            .map(UserGrowthAnalytics::getRetentionRate)
            .filter(rate -> rate != null && !rate.isZero())
            .mapToDouble(Percentage::getValue)
            .average()
            .orElse(0.0);
        
        return Percentage.of(avgRetention);
    }
    
    public Count getMostRecentTotalUsers() {
        return userGrowthAnalyticsRepository.findMostRecent()
            .map(UserGrowthAnalytics::getTotalUsers)
            .orElse(Count.zero());
    }
    
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }
}
