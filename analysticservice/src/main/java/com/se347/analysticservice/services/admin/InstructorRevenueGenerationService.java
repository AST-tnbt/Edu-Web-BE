package com.se347.analysticservice.services.admin;

import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.enums.Period;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Internal service for generating and calculating instructor revenue.
 * This is used by schedulers and internal processes, not for admin queries.
 */
public interface InstructorRevenueGenerationService {
    
    /**
     * Generates or updates instructor revenue for a specific period.
     * This is an internal operation used by schedulers.
     */
    InstructorRevenue generateInstructorRevenue(
        UUID instructorId,
        Period period,
        LocalDate startDate,
        LocalDate endDate
    );
}

