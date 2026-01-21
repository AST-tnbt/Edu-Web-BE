package com.se347.analysticservice.domains.services.revenue;

import com.se347.analysticservice.entities.admin.revenue.CourseRevenueSnapshot;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.InstructorCourseStatsRepository;
import com.se347.analysticservice.repositories.InstructorDailyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain Service for InstructorRevenue aggregate.
 * 
 * DDD PATTERN: Domain Service
 * 
 * RESPONSIBILITIES:
 * - Factory logic for creating InstructorRevenue with proper initial values
 * - Business rules for calculating instructor revenue metrics
 * - Aggregation logic for instructor revenue across courses
 * 
 * USAGE:
 * This service is called by Application Services when they need to create
 * or update InstructorRevenue entities with proper business rules.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorRevenueDomainService {
    
    private final InstructorDailyStatsRepository dailyStatsRepository;
    private final InstructorCourseStatsRepository courseStatsRepository;
    
    /**
     * Creates initial InstructorRevenue record for a period.
     * 
     * BUSINESS RULE:
     * - New InstructorRevenue starts with zero values
     * - Metrics will be calculated from actual course stats
     * 
     * @param instructorId the instructor ID
     * @param period the period (DAILY, WEEKLY, MONTHLY, YEARLY)
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @return new InstructorRevenue entity with zero initial values
     */
    @Transactional(readOnly = true)
    public InstructorRevenue createInitialInstructorRevenue(
        UUID instructorId, 
        Period period, 
        LocalDate startDate, 
        LocalDate endDate
    ) {
        log.debug("Creating initial InstructorRevenue: instructorId={}, period={}, startDate={}, endDate={}", 
                 instructorId, period, startDate, endDate);
        
        return InstructorRevenue.create(
            instructorId,
            period,
            startDate,
            endDate,
            Money.zero(),
            Count.zero(),
            Count.zero()
        );
    }
    
    /**
     * Calculates revenue metrics for an instructor in a period.
     * 
     * BUSINESS RULE:
     * - Total revenue: Sum of daily revenue from InstructorDailyStats in the period
     * - Total enrollments: Sum of new enrollments from InstructorDailyStats in the period
     * - Total courses: Count of courses from InstructorCourseStats that have revenue > 0
     * 
     * @param instructorId the instructor ID
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @return array with [totalRevenue, enrollmentsInPeriod, coursesInPeriod]
     */
    @Transactional(readOnly = true)
    public Object[] calculateRevenueMetrics(UUID instructorId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating revenue metrics: instructorId={}, startDate={}, endDate={}", 
                 instructorId, startDate, endDate);
        
        // Get daily stats for the period
        List<InstructorDailyStats> dailyStatsList = dailyStatsRepository
            .findByInstructorIdAndDateBetween(instructorId, startDate, endDate);
        
        // Calculate total revenue and enrollments from daily stats
        Money totalRevenue = dailyStatsList.stream()
            .map(InstructorDailyStats::getDailyRevenue)
            .reduce(Money.zero(), Money::add);
        
        long totalEnrollments = dailyStatsList.stream()
            .mapToLong(stats -> stats.getNewEnrollments().getValue())
            .sum();
        
        // Get all courses for the instructor that have revenue
        // Note: InstructorCourseStats doesn't have date, so we count all courses with revenue > 0
        List<InstructorCourseStats> courseStatsList = courseStatsRepository.findByInstructorId(instructorId);
        long totalCourses = courseStatsList.stream()
            .filter(stats -> stats.getTotalRevenue() != null && !stats.getTotalRevenue().isZero())
            .count();
        
        log.debug("Calculated metrics: totalRevenue={}, totalEnrollments={}, totalCourses={}", 
                 totalRevenue.getAmount(), totalEnrollments, totalCourses);
        
        return new Object[]{
            totalRevenue,
            Count.of(totalEnrollments),
            Count.of(totalCourses)
        };
    }
    
    /**
     * Populates top performing courses for an InstructorRevenue.
     * 
     * BUSINESS RULE:
     * - Gets top 5 courses by revenue from InstructorCourseStats
     * - Creates CourseRevenueSnapshot for each top course
     * - Note: Course title is set to "Course {courseId}" as placeholder
     *   (should be fetched from CourseService in production)
     * 
     * @param instructorRevenue the InstructorRevenue to populate
     * @param limit maximum number of top courses to include (default: 5)
     */
    @Transactional(readOnly = true)
    public void populateTopPerformingCourses(InstructorRevenue instructorRevenue, int limit) {
        if (instructorRevenue == null) {
            throw new IllegalArgumentException("InstructorRevenue cannot be null");
        }
        
        UUID instructorId = instructorRevenue.getInstructorId();
        
        // Get top courses by revenue
        List<InstructorCourseStats> topCourses = courseStatsRepository
            .findByInstructorIdOrderByRevenueDesc(instructorId)
            .stream()
            .filter(stats -> stats.getTotalRevenue() != null && !stats.getTotalRevenue().isZero())
            .limit(limit)
            .collect(Collectors.toList());
        
        log.debug("Populating top {} courses for instructor: {}", topCourses.size(), instructorId);
        
        // Clear existing top courses and add new ones
        instructorRevenue.getTopPerformingCourses().clear();
        
        for (InstructorCourseStats courseStats : topCourses) {
            // TODO: Fetch actual course title from CourseService
            // For now, use placeholder
            String courseTitle = "Course " + courseStats.getCourseId().toString().substring(0, 8);
            
            CourseRevenueSnapshot snapshot = CourseRevenueSnapshot.create(
                instructorRevenue,
                courseStats.getCourseId(),
                courseTitle,
                courseStats.getTotalStudents(),
                courseStats.getTotalRevenue()
            );
            
            instructorRevenue.addTopCourse(snapshot);
        }
        
        log.debug("Added {} top performing courses to InstructorRevenue", 
                 instructorRevenue.getTopPerformingCourses().size());
    }
}
