package com.se347.analysticservice.services.instructor;

import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.shared.valueobjects.Money;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorDailyStatsService {
    
    void recordEnrollment(UUID instructorId, LocalDate date);
    
    void recordActiveStudent(UUID instructorId, LocalDate date);
    
    void recordRevenue(UUID instructorId, LocalDate date, Money amount);
    
    void recordCourseCompletion(UUID instructorId, LocalDate date);
    
    Optional<InstructorDailyStats> getByInstructorIdAndDate(UUID instructorId, LocalDate date);
    
    List<InstructorDailyStats> getByInstructorId(UUID instructorId);
    
    List<InstructorDailyStats> getByInstructorIdAndDateBetween(UUID instructorId, LocalDate startDate, LocalDate endDate);
    
    List<InstructorDailyStats> getByInstructorIdOrderByDateDesc(UUID instructorId);
    
    InstructorDailyStats getOrCreate(UUID instructorId, LocalDate date);
}
