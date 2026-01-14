package com.se347.analysticservice.services.instructor.impls;

import com.se347.analysticservice.domains.services.shared.PercentageCalculationHelper;
import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.InstructorCourseStatsRepository;
import com.se347.analysticservice.repositories.InstructorOverviewRepository;
import com.se347.analysticservice.services.instructor.InstructorOverviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorOverviewServiceImpl implements InstructorOverviewService {
    
    private final InstructorOverviewRepository overviewRepository;
    private final InstructorCourseStatsRepository courseStatsRepository;
    
    @Override
    @Transactional
    public void recordCourse(UUID instructorId, UUID courseId) {
        log.debug("Recording course for instructor overview: instructorId={}, courseId={}", instructorId, courseId);
        
        InstructorOverview overview = getOrCreate(instructorId);
        
        overview.updateMetrics(
            overview.getTotalCourses().increment(),
            overview.getTotalStudents(),
            overview.getTotalRevenue(),
            overview.getAverageCompletionRate()
        );
        
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional
    public void recordEnrollment(UUID instructorId, Count count) {
        log.debug("Recording enrollment for instructor overview: instructorId={}, count={}", instructorId, count.getValue());
        
        InstructorOverview overview = getOrCreate(instructorId);
        
        overview.updateMetrics(
            overview.getTotalCourses(),
            overview.getTotalStudents().add(count),
            overview.getTotalRevenue(),
            overview.getAverageCompletionRate()
        );
        
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional
    public void recordRevenue(UUID instructorId, Money amount) {
        log.debug("Recording revenue for instructor overview: instructorId={}, amount={}", instructorId, amount.getAmount());
        
        InstructorOverview overview = getOrCreate(instructorId);
        
        overview.updateMetrics(
            overview.getTotalCourses(),
            overview.getTotalStudents(),
            overview.getTotalRevenue().add(amount),
            overview.getAverageCompletionRate()
        );
        
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional
    public void recalculateAverageCompletionRate(UUID instructorId) {
        log.debug("Recalculating average completion rate for instructor overview: instructorId={}", instructorId);
        
        InstructorOverview overview = getOrCreate(instructorId);
        
        List<InstructorCourseStats> courseStatsList = courseStatsRepository.findByInstructorId(instructorId);
        
        if (courseStatsList.isEmpty()) {
            overview.updateAverageCompletionRate(Percentage.zero());
            overviewRepository.save(overview);
            return;
        }
        
        List<Percentage> completionRates = courseStatsList.stream()
            .filter(cs -> cs.getCompletionRate() != null)
            .map(InstructorCourseStats::getCompletionRate)
            .collect(Collectors.toList());
        
        Percentage averageCompletionRate = PercentageCalculationHelper.calculateAverageFromPercentages(completionRates);
        overview.updateAverageCompletionRate(averageCompletionRate);
        
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorOverview> getByInstructorId(UUID instructorId) {
        return overviewRepository.findByInstructorId(instructorId);
    }
    
    @Override
    @Transactional
    public void recordEnrollmentCompletionRateUpdate(
        UUID instructorId, 
        UUID courseId, 
        UUID enrollmentId,
        Double previousEnrollmentRate, 
        Double newEnrollmentRate
    ) {
        log.debug("Recording enrollment completion rate update: instructorId={}, courseId={}, enrollmentId={}, " +
                  "previousRate={}, newRate={}", 
            instructorId, courseId, enrollmentId, previousEnrollmentRate, newEnrollmentRate);
        
        InstructorOverview overview = getOrCreate(instructorId);
        
        overview.recordEnrollmentCompletionRateUpdate(courseId, enrollmentId, previousEnrollmentRate, newEnrollmentRate);
        overviewRepository.save(overview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public InstructorOverview getOrCreate(UUID instructorId) {
        return overviewRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialOverview(instructorId));
    }
    
    private InstructorOverview createInitialOverview(UUID instructorId) {
        return InstructorOverview.create(
            instructorId,
            Count.zero(),
            Count.zero(),
            Money.zero(),
            Percentage.zero()
        );
    }
}
