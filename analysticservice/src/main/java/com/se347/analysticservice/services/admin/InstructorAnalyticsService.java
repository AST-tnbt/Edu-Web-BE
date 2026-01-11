package com.se347.analysticservice.services.admin;

import com.se347.analysticservice.entities.admin.instructor.InstructorStats;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.enums.Period;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorAnalyticsService {
    
    // ==================== Instructor Stats Queries ====================

    Optional<InstructorStats> getInstructorStats(UUID instructorId);

    List<InstructorStats> getTopInstructorsByStudents(int limit);

    List<InstructorStats> getTopInstructorsByCourses(int limit);
    
    // ==================== Revenue Queries ====================

    List<InstructorRevenue> getTopInstructorsByRevenue(Period period, LocalDate endDate, int limit);

    Optional<InstructorRevenue> getInstructorRevenue(
        UUID instructorId,
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    List<InstructorRevenue> getInstructorRevenueHistory(UUID instructorId, Period period);
}

