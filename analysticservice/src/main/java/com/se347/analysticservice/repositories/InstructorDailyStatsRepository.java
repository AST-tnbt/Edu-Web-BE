package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstructorDailyStatsRepository extends JpaRepository<InstructorDailyStats, UUID> {
    
    Optional<InstructorDailyStats> findByInstructorIdAndDate(UUID instructorId, LocalDate date);
    
    List<InstructorDailyStats> findByInstructorId(UUID instructorId);
    
    @Query("SELECT ids FROM InstructorDailyStats ids WHERE ids.instructorId = :instructorId AND ids.date BETWEEN :startDate AND :endDate ORDER BY ids.date ASC")
    List<InstructorDailyStats> findByInstructorIdAndDateBetween(
        @Param("instructorId") UUID instructorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT ids FROM InstructorDailyStats ids WHERE ids.instructorId = :instructorId ORDER BY ids.date DESC")
    List<InstructorDailyStats> findByInstructorIdOrderByDateDesc(@Param("instructorId") UUID instructorId);
    
    boolean existsByInstructorIdAndDate(UUID instructorId, LocalDate date);

    @Query("SELECT ids FROM InstructorDailyStats ids WHERE ids.date BETWEEN :startDate AND :endDate ORDER BY ids.date ASC")
    List<InstructorDailyStats> findByDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // ===== method với PESSIMISTIC_WRITE lock =====
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ids FROM InstructorDailyStats ids WHERE ids.instructorId = :instructorId AND ids.date = :date")
    Optional<InstructorDailyStats> findByInstructorIdAndDateWithLock(@Param("instructorId") UUID instructorId, @Param("date") LocalDate date);
}