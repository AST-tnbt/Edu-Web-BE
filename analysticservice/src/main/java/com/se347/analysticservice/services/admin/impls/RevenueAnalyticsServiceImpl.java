package com.se347.analysticservice.services.admin.impls;

import com.se347.analysticservice.domains.services.revenue.RevenueAggregationService;
import com.se347.analysticservice.entities.admin.revenue.DailyRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.repositories.DailyRevenueRepository;
import com.se347.analysticservice.services.admin.RevenueAnalyticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RevenueAnalyticsServiceImpl implements RevenueAnalyticsService {
    
    private final DailyRevenueRepository repository;
    private final RevenueAggregationService aggregationService;
    
    @Override
    @Transactional
    public void recordRevenue(BigDecimal amount, LocalDate transactionDate) {
        log.debug("Recording revenue: amount={}, date={}", amount, transactionDate);
        
        DailyRevenue dailyRevenue = repository.findByDate(transactionDate)
            .orElseGet(() -> createInitialDailyRevenue(transactionDate));
        
        Money newTotalRevenue = dailyRevenue.getTotalRevenue().add(Money.of(amount));
        Count newTotalTransactions = dailyRevenue.getTotalTransactions().increment();
        
        dailyRevenue.updateMetrics(newTotalRevenue, newTotalTransactions);
        
        repository.save(dailyRevenue);
    }
    
    @Override
    @Transactional
    public DailyRevenue generateDailyRevenue(LocalDate date) {
        log.info("Generating daily revenue for date: {}", date);
        
        Optional<DailyRevenue> existing = repository.findByDate(date);
        
        if (existing.isPresent()) {
            log.info("Daily revenue exists for date: {}", date);
            return existing.get();
        }
        
        DailyRevenue dailyRevenue = DailyRevenue.create(
            date,
            Money.zero(),
            Count.zero()
        );
        
        return repository.save(dailyRevenue);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<DailyRevenue> getDailyRevenue(LocalDate date) {
        return repository.findByDate(date);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DailyRevenue> getRevenueHistory(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateBetween(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Money getTotalRevenueForPeriod(LocalDate startDate, LocalDate endDate) {
        return aggregationService.getTotalRevenueInPeriod(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Money getAverageDailyRevenue(LocalDate startDate, LocalDate endDate) {
        return aggregationService.getAverageDailyRevenue(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Count getTotalTransactionsForPeriod(LocalDate startDate, LocalDate endDate) {
        return aggregationService.getTotalTransactionsInPeriod(startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DailyRevenue getHighestRevenueDay(LocalDate startDate, LocalDate endDate) {
        return aggregationService.getHighestRevenueDay(startDate, endDate);
    }
    
    @Override
    @Transactional
    public void recalculateDailyRevenue(LocalDate date) {
        log.info("Recalculating daily revenue for date: {}", date);
        
        DailyRevenue dailyRevenue = repository.findByDate(date)
            .orElseThrow(() -> new IllegalArgumentException("DailyRevenue not found for date: " + date));
        
        log.info("Daily revenue found: date={}, revenue={}, transactions={}", 
            date, dailyRevenue.getTotalRevenue(), dailyRevenue.getTotalTransactions().getValue());
    }
    
    private DailyRevenue createInitialDailyRevenue(LocalDate date) {
        return DailyRevenue.create(
            date,
            Money.zero(),
            Count.zero()
        );
    }
}

