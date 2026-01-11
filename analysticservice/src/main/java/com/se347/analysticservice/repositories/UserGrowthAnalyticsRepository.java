package com.se347.analysticservice.repositories;

import com.se347.analysticservice.entities.admin.platform.UserGrowthAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserGrowthAnalytics aggregate root.
 */
@Repository
public interface UserGrowthAnalyticsRepository extends JpaRepository<UserGrowthAnalytics, UUID> {
    
    /**
     * Finds analytics record by date.
     * @param date the date to search for
     * @return Optional containing the analytics if found
     */
    Optional<UserGrowthAnalytics> findByDate(LocalDate date);
    
    /**
     * Finds analytics records between date range.
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return List of analytics records
     */
    @Query("SELECT uga FROM UserGrowthAnalytics uga WHERE uga.date BETWEEN :startDate AND :endDate ORDER BY uga.date ASC")
    List<UserGrowthAnalytics> findByDateBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Finds the most recent analytics record.
     * @return Optional containing the most recent analytics
     */
    @Query("SELECT uga FROM UserGrowthAnalytics uga ORDER BY uga.date DESC LIMIT 1")
    Optional<UserGrowthAnalytics> findMostRecent();
    
    /**
     * Finds analytics for yesterday (used for retention calculation).
     * @param yesterday the date for yesterday
     * @return Optional containing yesterday's analytics
     */
    default Optional<UserGrowthAnalytics> findYesterdayAnalytics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return findByDate(yesterday);
    }
    
    /**
     * Checks if analytics exists for a specific date.
     * @param date the date to check
     * @return true if exists, false otherwise
     */
    boolean existsByDate(LocalDate date);
}

