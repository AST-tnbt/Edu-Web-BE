package com.se347.analysticservice.services.instructor;

import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorCourseStatsService {
    
    void recordEnrollment(UUID instructorId, UUID courseId, Count count);
    
    void updateOverallProgress(UUID instructorId, UUID courseId, UUID enrollmentId, double newOverallProgress);
    
    void recordRevenue(UUID instructorId, UUID courseId, Money amount);
    
    void updateCompletionRate(UUID instructorId, UUID courseId, Percentage completionRate);
    
    Optional<InstructorCourseStats> getByInstructorIdAndCourseId(UUID instructorId, UUID courseId);
    
    List<InstructorCourseStats> getByInstructorId(UUID instructorId);
    
    List<InstructorCourseStats> getByInstructorIdOrderByRevenueDesc(UUID instructorId);
    
    List<InstructorCourseStats> getByInstructorIdOrderByCompletionRateDesc(UUID instructorId);
    
    InstructorCourseStats getOrCreate(UUID instructorId, UUID courseId);
    
    void ensureCourseStatsExists(UUID instructorId, UUID courseId);
}
