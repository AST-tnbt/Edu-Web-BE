package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.admin.platform.PlatformOverview;
import com.se347.analysticservice.enums.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PlatformOverview aggregate root.
 */
@Repository
public interface PlatformOverviewRepository extends JpaRepository<PlatformOverview, UUID> {
    
    /**
     * Finds platform overview by period and date range.
     */
    Optional<PlatformOverview> findByPeriodAndStartDateAndEndDate(
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    /**
     * Finds the most recent overview for a given period.
     */
    @Query("SELECT po FROM PlatformOverview po WHERE po.period = :period ORDER BY po.endDate DESC LIMIT 1")
    Optional<PlatformOverview> findLatestByPeriod(@Param("period") Period period);
    
    /**
     * Finds all overviews for a given period, ordered by date descending.
     */
    @Query("SELECT po FROM PlatformOverview po WHERE po.period = :period ORDER BY po.endDate DESC")
    List<PlatformOverview> findAllByPeriodOrderByEndDateDesc(@Param("period") Period period);
    
    /**
     * Finds overviews within a date range for a given period.
     */
    @Query("SELECT po FROM PlatformOverview po WHERE po.period = :period AND po.endDate BETWEEN :startDate AND :endDate ORDER BY po.endDate DESC")
    List<PlatformOverview> findByPeriodAndDateRange(
        @Param("period") Period period,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Checks if an overview exists for the given period and date range.
     */
    boolean existsByPeriodAndStartDateAndEndDate(
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
    
    /**
     * Finds the previous period's overview (for growth rate calculation).
     */
    @Query("SELECT po FROM PlatformOverview po WHERE po.period = :period AND po.endDate < :currentEndDate ORDER BY po.endDate DESC LIMIT 1")
    Optional<PlatformOverview> findPreviousPeriod(
        @Param("period") Period period,
        @Param("currentEndDate") LocalDate currentEndDate
    );
}

