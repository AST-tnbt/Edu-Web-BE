package com.se347.analysticservice.services.instructor.impls;

import com.se347.analysticservice.domains.services.shared.PercentageCalculationHelper;
import com.se347.analysticservice.entities.instructor.InstructorCourseStats;
import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.repositories.InstructorCourseStatsRepository;
import com.se347.analysticservice.repositories.InstructorDailyStatsRepository;
import com.se347.analysticservice.repositories.InstructorOverviewRepository;
import com.se347.analysticservice.services.instructor.InstructorAnalyticsService;

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
    private final InstructorCourseStatsRepository courseStatsRepository;
    private final InstructorDailyStatsRepository dailyStatsRepository;

    // ==================== InstructorOverview Methods ====================

    @Override
    @Transactional
    public void recordCourseForInstructorOverview(UUID instructorId, UUID courseId) {
        log.debug("Recording course for instructor overview: instructorId={}, courseId={}", instructorId, courseId);
        
        InstructorOverview overview = overviewRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialOverview(instructorId));
        
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
    public void recordEnrollmentForInstructorOverview(UUID instructorId, Count count) {
        log.debug("Recording enrollment for instructor overview: instructorId={}, count={}", instructorId, count.getValue());
        
        InstructorOverview overview = overviewRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialOverview(instructorId));
        
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
    public void recordRevenueForInstructorOverview(UUID instructorId, Money amount) {
        log.debug("Recording revenue for instructor overview: instructorId={}, amount={}", instructorId, amount.getAmount());
        
        InstructorOverview overview = overviewRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialOverview(instructorId));
        
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
    public void recalculateInstructorOverviewAverageCompletionRate(UUID instructorId) {
        log.debug("Recalculating average completion rate for instructor overview: instructorId={}", instructorId);
        
        InstructorOverview overview = overviewRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialOverview(instructorId));
        
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
    public Optional<InstructorOverview> getInstructorOverview(UUID instructorId) {
        return overviewRepository.findByInstructorId(instructorId);
    }

    @Override
    @Transactional
    public void recordEnrollmentCompletionRateUpdate(UUID instructorId, UUID courseId, UUID enrollmentId,
                                                     Double previousEnrollmentRate, Double newEnrollmentRate) {
        log.debug("Recording enrollment completion rate update: instructorId={}, courseId={}, enrollmentId={}, " +
                  "previousRate={}, newRate={}", 
            instructorId, courseId, enrollmentId, previousEnrollmentRate, newEnrollmentRate);
        
        InstructorOverview overview = overviewRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialOverview(instructorId));
        
        overview.recordEnrollmentCompletionRateUpdate(courseId, enrollmentId, previousEnrollmentRate, newEnrollmentRate);
        overviewRepository.save(overview);
    }

    // ==================== InstructorCourseStats Methods ====================

    @Override
    @Transactional
    public void recordEnrollmentForCourse(UUID instructorId, UUID courseId, Count count) {
        log.debug("Recording enrollment for course: instructorId={}, courseId={}, count={}", 
            instructorId, courseId, count.getValue());
        
        InstructorCourseStats courseStats = courseStatsRepository
            .findByInstructorIdAndCourseId(instructorId, courseId)
            .orElseGet(() -> createInitialCourseStats(instructorId, courseId));
        
        courseStats.recordEnrollment(count);
        courseStatsRepository.save(courseStats);
        
        recalculateInstructorOverviewAverageCompletionRate(instructorId);
    }

    @Override
    @Transactional
    public void recordRevenueForCourse(UUID instructorId, UUID courseId, Money amount) {
        log.debug("Recording revenue for course: instructorId={}, courseId={}, amount={}", 
            instructorId, courseId, amount.getAmount());
        
        InstructorCourseStats courseStats = courseStatsRepository
            .findByInstructorIdAndCourseId(instructorId, courseId)
            .orElseGet(() -> createInitialCourseStats(instructorId, courseId));
        
        courseStats.recordRevenue(amount);
        courseStatsRepository.save(courseStats);
    }

    @Override
    @Transactional
    public void updateCourseCompletionRate(UUID instructorId, UUID courseId, Percentage completionRate) {
        log.debug("Updating course completion rate: instructorId={}, courseId={}, completionRate={}", 
            instructorId, courseId, completionRate.getValue());
        
        InstructorCourseStats courseStats = courseStatsRepository
            .findByInstructorIdAndCourseId(instructorId, courseId)
            .orElseGet(() -> createInitialCourseStats(instructorId, courseId));
        
        courseStats.updateCompletionRate(completionRate);
        courseStatsRepository.save(courseStats);
        
        recalculateInstructorOverviewAverageCompletionRate(instructorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorCourseStats> getCourseStats(UUID instructorId, UUID courseId) {
        return courseStatsRepository.findByInstructorIdAndCourseId(instructorId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstructorCourseStats> getCourseStatsByInstructor(UUID instructorId) {
        return courseStatsRepository.findByInstructorId(instructorId);
    }

    // ==================== InstructorDailyStats Methods ====================

    @Override
    @Transactional
    public void recordEnrollmentForDailyStats(UUID instructorId, LocalDate date) {
        log.debug("Recording enrollment for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = dailyStatsRepository
            .findByInstructorIdAndDate(instructorId, date)
            .orElseGet(() -> createInitialDailyStats(instructorId, date));
        
        dailyStats.recordEnrollment();
        dailyStatsRepository.save(dailyStats);
    }

    @Override
    @Transactional
    public void recordActiveStudentForDailyStats(UUID instructorId, LocalDate date) {
        log.debug("Recording active student for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = dailyStatsRepository
            .findByInstructorIdAndDate(instructorId, date)
            .orElseGet(() -> createInitialDailyStats(instructorId, date));
        
        dailyStats.recordActiveStudent();
        dailyStatsRepository.save(dailyStats);
    }

    @Override
    @Transactional
    public void recordRevenueForDailyStats(UUID instructorId, LocalDate date, Money amount) {
        log.debug("Recording revenue for daily stats: instructorId={}, date={}, amount={}", 
            instructorId, date, amount.getAmount());
        
        InstructorDailyStats dailyStats = dailyStatsRepository
            .findByInstructorIdAndDate(instructorId, date)
            .orElseGet(() -> createInitialDailyStats(instructorId, date));
        
        dailyStats.recordRevenue(amount);
        dailyStatsRepository.save(dailyStats);
    }

    @Override
    @Transactional
    public void recordCourseCompletionForDailyStats(UUID instructorId, LocalDate date) {
        log.debug("Recording course completion for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = dailyStatsRepository
            .findByInstructorIdAndDate(instructorId, date)
            .orElseGet(() -> createInitialDailyStats(instructorId, date));
        
        dailyStats.recordCourseCompletion();
        dailyStatsRepository.save(dailyStats);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorDailyStats> getDailyStats(UUID instructorId, LocalDate date) {
        return dailyStatsRepository.findByInstructorIdAndDate(instructorId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstructorDailyStats> getDailyStatsByInstructor(UUID instructorId, LocalDate startDate, LocalDate endDate) {
        return dailyStatsRepository.findByInstructorIdAndDateBetween(instructorId, startDate, endDate);
    }

    // ==================== Private Helper Methods ====================

    private InstructorOverview createInitialOverview(UUID instructorId) {
        return InstructorOverview.create(
            instructorId,
            Count.zero(),
            Count.zero(),
            Money.zero(),
            Percentage.zero()
        );
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

    private InstructorDailyStats createInitialDailyStats(UUID instructorId, LocalDate date) {
        return InstructorDailyStats.create(
            instructorId,
            date,
            Count.zero(),
            Count.zero(),
            Money.zero(),
            Count.zero()
        );
    }
}

