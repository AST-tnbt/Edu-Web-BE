package com.se347.analysticservice.domains.services.instructor;

import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.InstructorRevenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorRankingService {
    
    private final InstructorRevenueRepository instructorRevenueRepository;
    
    public List<InstructorRevenue> getTopInstructorsByRevenue(Period period, LocalDate endDate, int limit) {
        if (period == null || endDate == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        
        return instructorRevenueRepository.findByPeriodAndEndDate(period, endDate)
            .stream()
            .sorted(Comparator.comparing(
                (InstructorRevenue r) -> r.getTotalRevenue().getAmount()
            ).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<InstructorRevenue> getTopInstructorsByEnrollments(Period period, LocalDate endDate, int limit) {
        if (period == null || endDate == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        
        return instructorRevenueRepository.findByPeriodAndEndDate(period, endDate)
            .stream()
            .sorted(Comparator.comparing(
                (InstructorRevenue r) -> r.getTotalEnrollments().getValue()
            ).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public int calculateInstructorRank(UUID instructorId, Period period, LocalDate endDate) {
        if (instructorId == null || period == null || endDate == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        List<InstructorRevenue> allInstructors = instructorRevenueRepository
            .findByPeriodAndEndDate(period, endDate)
            .stream()
            .sorted(Comparator.comparing(
                (InstructorRevenue r) -> r.getTotalRevenue().getAmount()
            ).reversed())
            .collect(Collectors.toList());
        
        for (int i = 0; i < allInstructors.size(); i++) {
            if (allInstructors.get(i).getInstructorId().equals(instructorId)) {
                return i + 1;
            }
        }
        
        return -1;
    }
    
    public Money calculateAverageInstructorRevenue(Period period, LocalDate endDate) {
        if (period == null || endDate == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        List<InstructorRevenue> revenues = instructorRevenueRepository
            .findByPeriodAndEndDate(period, endDate);
        
        if (revenues.isEmpty()) {
            return Money.zero();
        }
        
        double total = revenues.stream()
            .mapToDouble(r -> r.getTotalRevenue().getAmount().doubleValue())
            .sum();
        
        double average = total / revenues.size();
        return Money.of(average);
    }
}

