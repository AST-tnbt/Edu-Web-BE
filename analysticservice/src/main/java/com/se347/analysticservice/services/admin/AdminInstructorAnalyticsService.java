package com.se347.analysticservice.services.admin;

import com.se347.analysticservice.entities.instructor.InstructorOverview;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminInstructorAnalyticsService {
    
    // ==================== Instructor Stats Queries ====================

    Optional<InstructorOverview> getInstructorStats(UUID instructorId);

    List<InstructorOverview> getTopInstructorsByStudents(int limit);

    List<InstructorOverview> getTopInstructorsByCourses(int limit);
    
    List<InstructorOverview> getTopInstructorsByRevenue(int limit);
}

