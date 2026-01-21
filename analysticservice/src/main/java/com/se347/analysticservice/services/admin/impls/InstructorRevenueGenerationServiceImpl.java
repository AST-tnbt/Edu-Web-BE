package com.se347.analysticservice.services.admin.impls;

import com.se347.analysticservice.domains.services.revenue.InstructorRevenueDomainService;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.InstructorRevenueRepository;
import com.se347.analysticservice.services.admin.InstructorRevenueGenerationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for Instructor Revenue Generation.
 * 
 * DDD PATTERN: Application Service
 * 
 * RESPONSIBILITIES:
 * - Orchestrate use cases (generate instructor revenue)
 * - Handle transaction boundaries
 * - Coordinate between repositories and domain services
 * 
 * BUSINESS LOGIC:
 * - Delegated to InstructorRevenueDomainService (factory and calculation logic)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorRevenueGenerationServiceImpl implements InstructorRevenueGenerationService {
    
    private final InstructorRevenueRepository revenueRepository;
    private final InstructorRevenueDomainService domainService;
    
    @Override
    @Transactional
    public InstructorRevenue generateInstructorRevenue(UUID instructorId, Period period, 
                                                      LocalDate startDate, LocalDate endDate) {
        log.info("Generating instructor revenue: instructorId={}, period={}, startDate={}, endDate={}", 
            instructorId, period, startDate, endDate);
        
        Optional<InstructorRevenue> existing = revenueRepository
            .findByInstructorIdAndPeriodAndStartDateAndEndDate(instructorId, period, startDate, endDate);
        
        if (existing.isPresent()) {
            log.info("Revenue exists, updating metrics");
            InstructorRevenue revenue = existing.get();
            
            // Calculate metrics using domain service
            Object[] metrics = domainService.calculateRevenueMetrics(instructorId, startDate, endDate);
            Money totalRevenue = (Money) metrics[0];
            Count enrollmentsInPeriod = (Count) metrics[1];
            Count coursesInPeriod = (Count) metrics[2];
            
            revenue.updateMetrics(totalRevenue, enrollmentsInPeriod, coursesInPeriod);
            
            // Populate top performing courses
            domainService.populateTopPerformingCourses(revenue, 5);
            
            return revenueRepository.save(revenue);
        }
        
        // Create using domain service (contains business rules)
        InstructorRevenue revenue = domainService.createInitialInstructorRevenue(
            instructorId, period, startDate, endDate
        );
        
        // Calculate and set metrics
        Object[] metrics = domainService.calculateRevenueMetrics(instructorId, startDate, endDate);
        Money totalRevenue = (Money) metrics[0];
        Count enrollmentsInPeriod = (Count) metrics[1];
        Count coursesInPeriod = (Count) metrics[2];
        
        revenue.updateMetrics(totalRevenue, enrollmentsInPeriod, coursesInPeriod);
        
        // Populate top performing courses
        domainService.populateTopPerformingCourses(revenue, 5);
        
        return revenueRepository.save(revenue);
    }
}

