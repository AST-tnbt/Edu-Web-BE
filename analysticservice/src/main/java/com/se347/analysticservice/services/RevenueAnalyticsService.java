package com.se347.analysticservice.services;

import com.se347.analysticservice.entities.admin.revenue.DailyRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RevenueAnalyticsService {
    
    void recordRevenue(BigDecimal amount, LocalDate transactionDate);
    
    DailyRevenue generateDailyRevenue(LocalDate date);
    
    Optional<DailyRevenue> getDailyRevenue(LocalDate date);
    
    List<DailyRevenue> getRevenueHistory(LocalDate startDate, LocalDate endDate);
    
    Money getTotalRevenueForPeriod(LocalDate startDate, LocalDate endDate);
    
    Money getAverageDailyRevenue(LocalDate startDate, LocalDate endDate);
    
    Count getTotalTransactionsForPeriod(LocalDate startDate, LocalDate endDate);
    
    DailyRevenue getHighestRevenueDay(LocalDate startDate, LocalDate endDate);
    
    void recalculateDailyRevenue(LocalDate date);
}

