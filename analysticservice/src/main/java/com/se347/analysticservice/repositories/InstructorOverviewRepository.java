package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.instructor.InstructorOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorOverviewRepository extends JpaRepository<InstructorOverview, UUID> {
    
    Optional<InstructorOverview> findByInstructorId(UUID instructorId);
    
    boolean existsByInstructorId(UUID instructorId);
    
    @Query("SELECT io FROM InstructorOverview io ORDER BY io.totalStudents.value DESC")
    List<InstructorOverview> findAllOrderByTotalStudentsDesc();
    
    @Query("SELECT io FROM InstructorOverview io ORDER BY io.totalCourses.value DESC")
    List<InstructorOverview> findAllOrderByTotalCoursesDesc();
}

