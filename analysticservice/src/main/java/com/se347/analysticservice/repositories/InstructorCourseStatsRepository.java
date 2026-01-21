package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorCourseStatsRepository extends JpaRepository<InstructorCourseStats, UUID> {
    
    Optional<InstructorCourseStats> findByInstructorIdAndCourseId(UUID instructorId, UUID courseId);
    
    List<InstructorCourseStats> findByInstructorId(UUID instructorId);
    
    @Query("SELECT ics FROM InstructorCourseStats ics WHERE ics.instructorId = :instructorId ORDER BY ics.totalRevenue.amount DESC")
    List<InstructorCourseStats> findByInstructorIdOrderByRevenueDesc(@Param("instructorId") UUID instructorId);
    
    @Query("SELECT ics FROM InstructorCourseStats ics WHERE ics.instructorId = :instructorId ORDER BY ics.completionRatePercent.value DESC")
    List<InstructorCourseStats> findByInstructorIdOrderByCompletionRateDesc(@Param("instructorId") UUID instructorId);
    
    boolean existsByInstructorIdAndCourseId(UUID instructorId, UUID courseId);
}

