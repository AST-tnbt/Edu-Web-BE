package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.enums.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorRevenueRepository extends JpaRepository<InstructorRevenue, UUID> {
    
    Optional<InstructorRevenue> findByInstructorIdAndPeriodAndStartDateAndEndDate(
        UUID instructorId, 
        Period period, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    List<InstructorRevenue> findByInstructorIdAndPeriod(UUID instructorId, Period period);
    
    @Query("SELECT ir FROM InstructorRevenue ir WHERE ir.period = :period AND ir.endDate = :endDate")
    List<InstructorRevenue> findByPeriodAndEndDate(@Param("period") Period period, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT ir FROM InstructorRevenue ir WHERE ir.instructorId = :instructorId ORDER BY ir.endDate DESC LIMIT 1")
    Optional<InstructorRevenue> findLatestByInstructorId(@Param("instructorId") UUID instructorId);
}

