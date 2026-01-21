package com.se347.analysticservice.domains.services.instructor;

import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain Service for InstructorDailyStats aggregate.
 * 
 * DDD PATTERN: Domain Service
 * 
 * RESPONSIBILITIES:
 * - Factory logic for creating InstructorDailyStats with proper initial values
 * - Business rules for initializing daily stats
 * 
 * USAGE:
 * This service is called by Application Services when they need to create
 * InstructorDailyStats entities with proper business rules.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorDailyStatsDomainService {
    
    /**
     * Creates initial InstructorDailyStats record.
     * 
     * BUSINESS RULE:
     * - New daily stats starts with zero values for all metrics
     * 
     * @param instructorId the instructor ID
     * @param date the date for the stats
     * @return new InstructorDailyStats entity with zero initial values
     */
    @Transactional(readOnly = true)
    public InstructorDailyStats createInitialDailyStats(UUID instructorId, LocalDate date) {
        log.debug("Creating initial InstructorDailyStats: instructorId={}, date={}", 
                 instructorId, date);
        
        return InstructorDailyStats.create(
            instructorId,
            date,
            Count.zero(), // newEnrollments
            Count.zero(), // activeStudents
            Money.zero(), // revenue
            Count.zero()  // completedCourses
        );
    }
}
