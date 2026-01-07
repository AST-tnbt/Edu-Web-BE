package com.se347.analysticservice.services.impls;

import com.se347.analysticservice.domains.services.instructor.InstructorRankingService;
import com.se347.analysticservice.entities.admin.instructor.InstructorStats;
import com.se347.analysticservice.entities.admin.revenue.CourseRevenueSnapshot;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.InstructorRevenueRepository;
import com.se347.analysticservice.repositories.InstructorStatsRepository;
import com.se347.analysticservice.services.InstructorAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorAnalyticsServiceImpl implements InstructorAnalyticsService {
    
    private final InstructorStatsRepository statsRepository;
    private final InstructorRevenueRepository revenueRepository;
    private final InstructorRankingService rankingService;
    
    @Override
    @Transactional
    public void recordCourseAddedToInstructor(UUID instructorId, UUID courseId) {
        log.debug("Recording course added to instructor: instructorId={}, courseId={}", 
            instructorId, courseId);
        
        InstructorStats stats = statsRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialStats(instructorId));
        
        stats.updateMetrics(
            stats.getTotalCourses().increment(),
            stats.getTotalStudents()
        );
        
        statsRepository.save(stats);
    }
    
    @Override
    @Transactional
    public void recordStudentsAddedToInstructor(UUID instructorId, Count newStudents) {
        log.debug("Recording students added to instructor: instructorId={}, newStudents={}", 
            instructorId, newStudents.getValue());
        
        InstructorStats stats = statsRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialStats(instructorId));
        
        stats.updateMetrics(
            stats.getTotalCourses(),
            stats.getTotalStudents().add(newStudents)
        );
        
        statsRepository.save(stats);
    }
    
    @Override
    @Transactional
    public void updateInstructorStats(UUID instructorId, Count totalCourses, Count totalStudents) {
        log.debug("Updating instructor stats: instructorId={}, courses={}, students={}", 
            instructorId, totalCourses.getValue(), totalStudents.getValue());
        
        InstructorStats stats = statsRepository.findByInstructorId(instructorId)
            .orElseGet(() -> createInitialStats(instructorId));
        
        stats.updateMetrics(totalCourses, totalStudents);
        statsRepository.save(stats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorStats> getInstructorStats(UUID instructorId) {
        return statsRepository.findByInstructorId(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorStats> getTopInstructorsByStudents(int limit) {
        return statsRepository.findAllOrderByTotalStudentsDesc()
            .stream()
            .limit(limit)
            .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorStats> getTopInstructorsByCourses(int limit) {
        return statsRepository.findAllOrderByTotalCoursesDesc()
            .stream()
            .limit(limit)
            .toList();
    }
    
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
    
    @Override
    @Transactional(readOnly = true)
    public int getInstructorRank(UUID instructorId, Period period, LocalDate endDate) {
        return rankingService.calculateInstructorRank(instructorId, period, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Money getAverageInstructorRevenue(Period period, LocalDate endDate) {
        return rankingService.calculateAverageInstructorRevenue(period, endDate);
    }
    
    @Override
    @Transactional
    public void addTopPerformingCourse(UUID instructorId, Period period, UUID courseId, 
                                       String courseTitle, Count enrollmentCount, Money revenue) {
        log.debug("Adding top performing course: instructorId={}, courseId={}, revenue={}", 
            instructorId, courseId, revenue);
        
        Optional<InstructorRevenue> revenueOpt = revenueRepository
            .findLatestByInstructorId(instructorId);
        
        if (revenueOpt.isEmpty()) {
            log.warn("No revenue record found for instructor: {}", instructorId);
            return;
        }
        
        InstructorRevenue instructorRevenue = revenueOpt.get();
        
        CourseRevenueSnapshot snapshot = CourseRevenueSnapshot.create(
            instructorRevenue,
            courseId,
            courseTitle,
            enrollmentCount,
            revenue
        );
        
        instructorRevenue.addTopCourse(snapshot);
        revenueRepository.save(instructorRevenue);
    }
    
    private InstructorStats createInitialStats(UUID instructorId) {
        return InstructorStats.create(
            instructorId,
            Count.zero(),
            Count.zero()
        );
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

