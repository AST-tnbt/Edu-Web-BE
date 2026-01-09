package com.se347.analysticservice.services.admin.impls;

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

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorRevenueGenerationServiceImpl implements InstructorRevenueGenerationService {
    
    private final InstructorRevenueRepository revenueRepository;
    
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
            updateRevenueMetrics(revenue, instructorId, startDate, endDate);
            return revenueRepository.save(revenue);
        }
        
        InstructorRevenue revenue = createNewRevenue(instructorId, period, startDate, endDate);
        return revenueRepository.save(revenue);
    }
    
    private InstructorRevenue createNewRevenue(UUID instructorId, Period period, 
                                               LocalDate startDate, LocalDate endDate) {
        Count coursesInPeriod = Count.zero();
        Count enrollmentsInPeriod = Count.zero();
        Money totalRevenue = Money.zero();
        
        return InstructorRevenue.create(
            instructorId,
            period,
            startDate,
            endDate,
            totalRevenue,
            enrollmentsInPeriod,
            coursesInPeriod
        );
    }
    
    private void updateRevenueMetrics(InstructorRevenue revenue, UUID instructorId, 
                                     LocalDate startDate, LocalDate endDate) {
        Count coursesInPeriod = Count.zero();
        Count enrollmentsInPeriod = Count.zero();
        Money totalRevenue = Money.zero();
        
        revenue.updateMetrics(totalRevenue, enrollmentsInPeriod, coursesInPeriod);
    }
}

