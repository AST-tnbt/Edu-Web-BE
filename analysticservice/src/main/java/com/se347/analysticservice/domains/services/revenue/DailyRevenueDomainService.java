package com.se347.analysticservice.domains.services.revenue;

import com.se347.analysticservice.entities.admin.revenue.DailyRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Domain Service for DailyRevenue aggregate.
 * 
 * DDD PATTERN: Domain Service
 * 
 * RESPONSIBILITIES:
 * - Factory logic for creating DailyRevenue with proper initial values
 * - Business rules for initializing revenue records
 * 
 * USAGE:
 * This service is called by Application Services when they need to create
 * DailyRevenue entities with proper business rules.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DailyRevenueDomainService {
    
    /**
     * Creates initial DailyRevenue record for a date.
     * 
     * BUSINESS RULE:
     * - New DailyRevenue starts with zero revenue and zero transactions
     * 
     * @param date the date for the revenue record
     * @return new DailyRevenue entity with zero initial values
     */
    @Transactional(readOnly = true)
    public DailyRevenue createInitialDailyRevenue(LocalDate date) {
        log.debug("Creating initial DailyRevenue for date={}", date);
        return DailyRevenue.create(
            date,
            Money.zero(),
            Count.zero()
        );
    }
}
