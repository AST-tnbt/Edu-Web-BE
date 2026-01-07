package com.se347.analysticservice.services;

import com.se347.analysticservice.entities.admin.instructor.InstructorStats;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.enums.Period;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorAnalyticsService {
    
    void recordCourseAddedToInstructor(UUID instructorId, UUID courseId);
    
    void recordStudentsAddedToInstructor(UUID instructorId, Count newStudents);
    
    void updateInstructorStats(UUID instructorId, Count totalCourses, Count totalStudents);
    
    Optional<InstructorStats> getInstructorStats(UUID instructorId);
    
    List<InstructorStats> getTopInstructorsByStudents(int limit);
    
    List<InstructorStats> getTopInstructorsByCourses(int limit);
    
    InstructorRevenue generateInstructorRevenue(
        UUID instructorId,
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    Optional<InstructorRevenue> getInstructorRevenue(
        UUID instructorId,
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    List<InstructorRevenue> getInstructorRevenueHistory(UUID instructorId, Period period);
    
    List<InstructorRevenue> getTopInstructorsByRevenue(Period period, LocalDate endDate, int limit);
    
    int getInstructorRank(UUID instructorId, Period period, LocalDate endDate);
    
    Money getAverageInstructorRevenue(Period period, LocalDate endDate);
    
    void addTopPerformingCourse(
        UUID instructorId,
        Period period,
        UUID courseId,
        String courseTitle,
        Count enrollmentCount,
        Money revenue
    );
}

