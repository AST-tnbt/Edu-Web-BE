package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.instructor.InstructorOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

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

    @Query("SELECT io FROM InstructorOverview io ORDER BY io.totalRevenue.amount DESC")
    List<InstructorOverview> findAllOrderByTotalRevenueDesc();

    // ===== method với PESSIMISTIC_WRITE lock =====
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT io FROM InstructorOverview io WHERE io.instructorId = :instructorId")
    Optional<InstructorOverview> findByInstructorIdWithLock(@Param("instructorId") UUID instructorId);
}

