package com.se347.analysticservice.domains.services.instructor;

import com.se347.analysticservice.domains.services.shared.PercentageCalculationHelper;
import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.InstructorCourseStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain Service for InstructorOverview aggregate.
 * 
 * DDD PATTERN: Domain Service
 * 
 * RESPONSIBILITIES:
 * - Factory logic for creating InstructorOverview with proper initial values
 * - Business rules for calculating average completion rate
 * - Cross-aggregate logic for instructor overview
 * 
 * USAGE:
 * This service is called by Application Services when they need to create
 * InstructorOverview entities or calculate derived metrics.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorOverviewDomainService {
    
    private final InstructorCourseStatsRepository courseStatsRepository;
    
    /**
     * Creates initial InstructorOverview record.
     * 
     * BUSINESS RULE:
     * - New overview starts with zero courses, zero students, zero revenue, zero completion rate
     * 
     * @param instructorId the instructor ID
     * @return new InstructorOverview entity with zero initial values
     */
    @Transactional(readOnly = true)
    public InstructorOverview createInitialOverview(UUID instructorId) {
        log.debug("Creating initial InstructorOverview: instructorId={}", instructorId);
        
        return InstructorOverview.create(
            instructorId,
            Count.zero(),
            Count.zero(),
            Money.zero(),
            Percentage.zero()
        );
    }
    
    /**
     * Calculates average completion rate from course stats.
     * 
     * BUSINESS RULE:
     * - Average = sum of all course completion rates / number of courses with completion rates
     * - If no courses have completion rates, return zero
     * 
     * @param instructorId the instructor ID
     * @return calculated average completion rate
     */
    @Transactional(readOnly = true)
    public Percentage calculateAverageCompletionRate(UUID instructorId) {
        log.debug("Calculating average completion rate: instructorId={}", instructorId);
        
        List<InstructorCourseStats> courseStatsList = courseStatsRepository.findByInstructorId(instructorId);
        
        if (courseStatsList.isEmpty()) {
            log.debug("No course stats found, returning zero completion rate");
            return Percentage.zero();
        }
        
        List<Percentage> completionRates = courseStatsList.stream()
            .filter(cs -> cs.getCompletionRate() != null && !cs.getCompletionRate().isZero())
            .map(InstructorCourseStats::getCompletionRate)
            .collect(Collectors.toList());
        
        if (completionRates.isEmpty()) {
            log.debug("No completion rates found, returning zero");
            return Percentage.zero();
        }
        
        Percentage average = PercentageCalculationHelper.calculateAverageFromPercentages(completionRates);
        log.debug("Calculated average completion rate: {}%", average.getValue());
        return average;
    }
}
