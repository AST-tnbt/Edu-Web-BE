package com.se347.analysticservice.services.admin.impls;

import com.se347.analysticservice.domains.services.instructor.InstructorRankingService;
import com.se347.analysticservice.entities.admin.instructor.InstructorStats;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.InstructorOverviewRepository;
import com.se347.analysticservice.repositories.InstructorRevenueRepository;
import com.se347.analysticservice.services.admin.InstructorAnalyticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorAnalyticsServiceImpl implements InstructorAnalyticsService {
    
    private final InstructorOverviewRepository overviewRepository;
    private final InstructorRevenueRepository revenueRepository;
    private final InstructorRankingService rankingService;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorStats> getInstructorStats(UUID instructorId) {
        return overviewRepository.findByInstructorId(instructorId)
            .map(this::toInstructorStats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorStats> getTopInstructorsByStudents(int limit) {
        return overviewRepository.findAllOrderByTotalStudentsDesc()
            .stream()
            .limit(limit)
            .map(this::toInstructorStats)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorStats> getTopInstructorsByCourses(int limit) {
        return overviewRepository.findAllOrderByTotalCoursesDesc()
            .stream()
            .limit(limit)
            .map(this::toInstructorStats)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorRevenue> getInstructorRevenue(UUID instructorId, Period period, 
                                                            LocalDate startDate, LocalDate endDate) {
        return revenueRepository.findByInstructorIdAndPeriodAndStartDateAndEndDate(
            instructorId, period, startDate, endDate
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorRevenue> getInstructorRevenueHistory(UUID instructorId, Period period) {
        return revenueRepository.findByInstructorIdAndPeriod(instructorId, period);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorRevenue> getTopInstructorsByRevenue(Period period, LocalDate endDate, int limit) {
        return rankingService.getTopInstructorsByRevenue(period, endDate, limit);
    }
    
    private InstructorStats toInstructorStats(InstructorOverview overview) {
        return InstructorStats.fromInstructorOverview(
            overview.getInstructorId(),
            overview.getTotalCourses(),
            overview.getTotalStudents(),
            overview.getAverageCompletionRate() != null 
                ? overview.getAverageCompletionRate() 
                : com.se347.analysticservice.entities.shared.valueobjects.Percentage.zero()
        );
    }
}

