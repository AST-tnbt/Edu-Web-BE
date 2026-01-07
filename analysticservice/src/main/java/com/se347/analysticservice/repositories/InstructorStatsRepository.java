package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.admin.instructor.InstructorStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorStatsRepository extends JpaRepository<InstructorStats, UUID> {
    
    Optional<InstructorStats> findByInstructorId(UUID instructorId);
    
    boolean existsByInstructorId(UUID instructorId);
    
    @Query("SELECT i FROM InstructorStats i ORDER BY i.totalStudents.value DESC")
    List<InstructorStats> findAllOrderByTotalStudentsDesc();
    
    @Query("SELECT i FROM InstructorStats i ORDER BY i.totalCourses.value DESC")
    List<InstructorStats> findAllOrderByTotalCoursesDesc();
}

