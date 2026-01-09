package com.se347.analysticservice.services.instructor;

import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorAnalyticsService {
    // ==================== InstructorOverview Methods ====================
    
    void recordCourseForInstructorOverview(UUID instructorId, UUID courseId);
    
    void recordEnrollmentForInstructorOverview(UUID instructorId, Count count);
    
    void recordRevenueForInstructorOverview(UUID instructorId, Money amount);
    
    void recalculateInstructorOverviewAverageCompletionRate(UUID instructorId);
    
    Optional<InstructorOverview> getInstructorOverview(UUID instructorId);
    
    void recordEnrollmentCompletionRateUpdate(UUID instructorId, UUID courseId, UUID enrollmentId, 
                                              Double previousEnrollmentRate, Double newEnrollmentRate);
    
    // ==================== InstructorCourseStats Methods ====================
    
    void recordEnrollmentForCourse(UUID instructorId, UUID courseId, Count count);
    
    void recordRevenueForCourse(UUID instructorId, UUID courseId, Money amount);
    
    void updateCourseCompletionRate(UUID instructorId, UUID courseId, Percentage completionRate);
    
    Optional<InstructorCourseStats> getCourseStats(UUID instructorId, UUID courseId);
    
    List<InstructorCourseStats> getCourseStatsByInstructor(UUID instructorId);
    
    // ==================== InstructorDailyStats Methods ====================
    
    void recordEnrollmentForDailyStats(UUID instructorId, LocalDate date);
    
    void recordActiveStudentForDailyStats(UUID instructorId, LocalDate date);
    
    void recordRevenueForDailyStats(UUID instructorId, LocalDate date, Money amount);
    
    void recordCourseCompletionForDailyStats(UUID instructorId, LocalDate date);
    
    Optional<InstructorDailyStats> getDailyStats(UUID instructorId, LocalDate date);
    
    List<InstructorDailyStats> getDailyStatsByInstructor(UUID instructorId, LocalDate startDate, LocalDate endDate);
}

