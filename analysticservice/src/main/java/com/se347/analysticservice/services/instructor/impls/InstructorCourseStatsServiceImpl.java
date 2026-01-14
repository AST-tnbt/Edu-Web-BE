package com.se347.analysticservice.services.instructor.impls;

import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.InstructorCourseStatsRepository;
import com.se347.analysticservice.services.instructor.InstructorCourseStatsService;
import com.se347.analysticservice.services.instructor.InstructorOverviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorCourseStatsServiceImpl implements InstructorCourseStatsService {
    
    private final InstructorCourseStatsRepository courseStatsRepository;
    private final InstructorOverviewService instructorOverviewService;
    
    @Override
    @Transactional
    public void recordEnrollment(UUID instructorId, UUID courseId, Count count) {
        log.debug("Recording enrollment for course: instructorId={}, courseId={}, count={}", 
            instructorId, courseId, count.getValue());
        
        InstructorCourseStats courseStats = getOrCreate(instructorId, courseId);
        
        courseStats.recordEnrollment(count);
        courseStatsRepository.save(courseStats);
    }
    
    @Override
    @Transactional
    public void recordRevenue(UUID instructorId, UUID courseId, Money amount) {
        log.debug("Recording revenue for course: instructorId={}, courseId={}, amount={}", 
            instructorId, courseId, amount.getAmount());
        
        InstructorCourseStats courseStats = getOrCreate(instructorId, courseId);
        
        courseStats.recordRevenue(amount);
        courseStatsRepository.save(courseStats);
        
        instructorOverviewService.recordRevenue(instructorId, amount);
    }
    
    @Override
    @Transactional
    public void updateCompletionRate(UUID instructorId, UUID courseId, Percentage completionRate) {
        log.debug("Updating course completion rate: instructorId={}, courseId={}, completionRate={}", 
            instructorId, courseId, completionRate.getValue());
        
        InstructorCourseStats courseStats = getOrCreate(instructorId, courseId);
        
        courseStats.updateCompletionRate(completionRate);
        courseStatsRepository.save(courseStats);
        
        instructorOverviewService.recalculateAverageCompletionRate(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorCourseStats> getByInstructorIdAndCourseId(UUID instructorId, UUID courseId) {
        return courseStatsRepository.findByInstructorIdAndCourseId(instructorId, courseId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourseStats> getByInstructorId(UUID instructorId) {
        return courseStatsRepository.findByInstructorId(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourseStats> getByInstructorIdOrderByRevenueDesc(UUID instructorId) {
        return courseStatsRepository.findByInstructorIdOrderByRevenueDesc(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourseStats> getByInstructorIdOrderByCompletionRateDesc(UUID instructorId) {
        return courseStatsRepository.findByInstructorIdOrderByCompletionRateDesc(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public InstructorCourseStats getOrCreate(UUID instructorId, UUID courseId) {
        return courseStatsRepository.findByInstructorIdAndCourseId(instructorId, courseId)
            .orElseGet(() -> createInitialCourseStats(instructorId, courseId));
    }
    
    @Override
    @Transactional
    public void ensureCourseStatsExists(UUID instructorId, UUID courseId) {
        if (!courseStatsRepository.existsByInstructorIdAndCourseId(instructorId, courseId)) {
            InstructorCourseStats courseStats = createInitialCourseStats(instructorId, courseId);
            courseStatsRepository.save(courseStats);
        }
    }
    
    private InstructorCourseStats createInitialCourseStats(UUID instructorId, UUID courseId) {
        return InstructorCourseStats.create(
            instructorId,
            courseId,
            Count.zero(),
            Money.zero(),
            Percentage.zero()
        );
    }
}
