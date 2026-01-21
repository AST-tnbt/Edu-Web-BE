package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.admin.revenue.DailyRevenue;
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
public interface DailyRevenueRepository extends JpaRepository<DailyRevenue, UUID> {


    Optional<DailyRevenue> findByDate(LocalDate date);

    @Query("SELECT dr FROM DailyRevenue dr WHERE dr.date BETWEEN :startDate AND :endDate ORDER BY dr.date ASC")
    List<DailyRevenue> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT dr FROM DailyRevenue dr ORDER BY dr.date DESC LIMIT 1")
    Optional<DailyRevenue> findMostRecent();
    
    boolean existsByDate(LocalDate date);

    // ===== method với PESSIMISTIC_WRITE lock =====
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dr FROM DailyRevenue dr WHERE dr.date = :date")
    Optional<DailyRevenue> findByDateWithLock(@Param("date") LocalDate date);
}

