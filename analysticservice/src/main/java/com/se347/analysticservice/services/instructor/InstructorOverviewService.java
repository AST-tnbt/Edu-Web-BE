package com.se347.analysticservice.services.instructor;

import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;

import java.util.Optional;
import java.util.UUID;

public interface InstructorOverviewService {
    
    void recordCourse(UUID instructorId, UUID courseId);
    
    void recordEnrollment(UUID instructorId, Count count);
    
    void recordRevenue(UUID instructorId, Money amount);
    
    void recalculateAverageCompletionRate(UUID instructorId);
    
    Optional<InstructorOverview> getByInstructorId(UUID instructorId);
    
    void recordEnrollmentCompletionRateUpdate(
        UUID instructorId, 
        UUID courseId, 
        UUID enrollmentId,
        Double previousEnrollmentRate, 
        Double newEnrollmentRate
    );
    
    void recalculateInstructorOverview(UUID instructorId);
}
