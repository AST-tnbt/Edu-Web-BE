package com.se347.analysticservice.domains.services.revenue;

import com.se347.analysticservice.entities.admin.revenue.DailyRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.repositories.DailyRevenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueAggregationService {
    
    private final DailyRevenueRepository dailyRevenueRepository;
    
    public Money getTotalRevenueInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<DailyRevenue> revenues = dailyRevenueRepository
            .findByDateBetween(startDate, endDate);
        
        BigDecimal total = revenues.stream()
            .map(r -> r.getTotalRevenue().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Money.of(total);
    }
    
    public Count getTotalTransactionsInPeriod(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<DailyRevenue> revenues = dailyRevenueRepository
            .findByDateBetween(startDate, endDate);
        
        long total = revenues.stream()
            .mapToLong(r -> r.getTotalTransactions().getValue())
            .sum();
        
        return Count.of(total);
    }
    
    public Money getAverageDailyRevenue(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<DailyRevenue> revenues = dailyRevenueRepository
            .findByDateBetween(startDate, endDate);
        
        if (revenues.isEmpty()) {
            return Money.zero();
        }
        
        BigDecimal total = revenues.stream()
            .map(r -> r.getTotalRevenue().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal average = total.divide(
            BigDecimal.valueOf(revenues.size()), 
            2, 
            java.math.RoundingMode.HALF_UP
        );
        
        return Money.of(average);
    }
    
    public DailyRevenue getHighestRevenueDay(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        
        List<DailyRevenue> revenues = dailyRevenueRepository
            .findByDateBetween(startDate, endDate);
        
        return revenues.stream()
            .max((r1, r2) -> r1.getTotalRevenue().getAmount()
                .compareTo(r2.getTotalRevenue().getAmount()))
            .orElse(null);
    }
    
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }
}

