package com.se347.analysticservice.domains.services.instructor;

import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Domain Service for InstructorCourseStats aggregate.
 * 
 * DDD PATTERN: Domain Service
 * 
 * RESPONSIBILITIES:
 * - Factory logic for creating InstructorCourseStats with proper initial values
 * - Business rules for initializing course stats
 * 
 * USAGE:
 * This service is called by Application Services when they need to create
 * InstructorCourseStats entities with proper business rules.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorCourseStatsDomainService {
    
    /**
     * Creates initial InstructorCourseStats record.
     * 
     * BUSINESS RULE:
     * - New course stats starts with zero students, zero revenue, zero completion rate
     * 
     * @param instructorId the instructor ID
     * @param courseId the course ID
     * @return new InstructorCourseStats entity with zero initial values
     */
    @Transactional(readOnly = true)
    public InstructorCourseStats createInitialCourseStats(UUID instructorId, UUID courseId) {
        log.debug("Creating initial InstructorCourseStats: instructorId={}, courseId={}", 
                 instructorId, courseId);
        
        return InstructorCourseStats.create(
            instructorId,
            courseId,
            Count.zero(),
            Money.zero(),
            Percentage.zero()
        );
    }
}
