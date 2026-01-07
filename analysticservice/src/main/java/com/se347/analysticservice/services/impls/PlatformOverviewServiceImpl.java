package com.se347.analysticservice.services.impls;

import com.se347.analysticservice.domains.services.platform.PlatformMetricsAggregationService;
import com.se347.analysticservice.domains.services.platform.PlatformMetricsCalculationService;
import com.se347.analysticservice.entities.admin.platform.PlatformOverview;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import com.se347.analysticservice.enums.Period;
import com.se347.analysticservice.repositories.PlatformOverviewRepository;
import com.se347.analysticservice.services.PlatformOverviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlatformOverviewServiceImpl implements PlatformOverviewService {
    
    private final PlatformOverviewRepository repository;
    private final PlatformMetricsAggregationService aggregationService;
    private final PlatformMetricsCalculationService calculationService;
    
    @Override
    @Transactional
    public void recordCourseCreation(UUID courseId, UUID instructorId, LocalDate createdDate) {
        log.debug("Course created recorded: courseId={}, instructorId={}, date={}", 
            courseId, instructorId, createdDate);
    }
    
    @Override
    @Transactional
    public void recordCoursePublication(UUID courseId, UUID instructorId, LocalDate publishedDate) {
        log.debug("Course published recorded: courseId={}, instructorId={}, date={}", 
            courseId, instructorId, publishedDate);
    }
    
    @Override
    @Transactional
    public void recordEnrollment(UUID enrollmentId, UUID studentId, UUID courseId, 
                                 UUID instructorId, LocalDate enrolledDate) {
        log.debug("Enrollment recorded: enrollmentId={}, studentId={}, courseId={}, date={}", 
            enrollmentId, studentId, courseId, enrolledDate);
    }
    
    @Override
    @Transactional
    public void recordPayment(UUID paymentId, UUID courseId,
                             BigDecimal amount, LocalDate paymentDate) {
        log.debug("Payment recorded: paymentId={}, amount={}, date={}", 
            paymentId, amount, paymentDate);
    }
    
    @Override
    @Transactional
    public void recordCourseCompletion(UUID studentId, UUID courseId, UUID instructorId, 
                                       UUID enrollmentId, LocalDate completedDate) {
        log.debug("Course completion recorded: studentId={}, courseId={}, date={}", 
            studentId, courseId, completedDate);
    }
    
    @Override
    @Transactional
    public void recordProgressUpdate(UUID studentId, UUID courseId, UUID instructorId, 
                                     Double completionRate, LocalDate updateDate) {
        log.debug("Progress update recorded: studentId={}, courseId={}, rate={}%", 
            studentId, courseId, completionRate);
    }
    
    @Override
    @Transactional
    public PlatformOverview generatePlatformOverview(Period period, LocalDate startDate, LocalDate endDate) {
        log.info("Generating platform overview: period={}, startDate={}, endDate={}", 
            period, startDate, endDate);
        
        Optional<PlatformOverview> existing = repository
            .findByPeriodAndStartDateAndEndDate(period, startDate, endDate);
        
        if (existing.isPresent()) {
            log.info("Overview exists, updating metrics");
            PlatformOverview overview = existing.get();
            updateOverviewMetrics(overview, startDate, endDate);
            return repository.save(overview);
        }
        
        PlatformOverview overview = createNewOverview(period, startDate, endDate);
        return repository.save(overview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PlatformOverview> getLatestOverview(Period period) {
        return repository.findLatestByPeriod(period);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PlatformOverview> getOverviewForPeriod(Period period, LocalDate startDate, LocalDate endDate) {
        return repository.findByPeriodAndStartDateAndEndDate(period, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PlatformOverview> getOverviewHistory(Period period, int limit) {
        return repository.findAllByPeriodOrderByEndDateDesc(period)
            .stream()
            .limit(limit)
            .toList();
    }
    
    @Override
    @Transactional
    public void recalculateOverview(UUID overviewId) {
        log.info("Recalculating overview: id={}", overviewId);
        
        PlatformOverview overview = repository.findById(overviewId)
            .orElseThrow(() -> new IllegalArgumentException("PlatformOverview not found: " + overviewId));
        
        updateOverviewMetrics(overview, overview.getStartDate(), overview.getEndDate());
        repository.save(overview);
    }
    
    @Override
    @Transactional
    public PlatformOverview initializeCurrentPeriodOverview(Period period) {
        LocalDate[] dates = calculatePeriodDates(period);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];
        
        Optional<PlatformOverview> existing = repository
            .findByPeriodAndStartDateAndEndDate(period, startDate, endDate);
        
        if (existing.isPresent()) {
            log.info("Current period overview already exists: period={}", period);
            return existing.get();
        }
        
        log.info("Initializing new period overview: period={}, startDate={}, endDate={}", 
            period, startDate, endDate);
        
        return generatePlatformOverview(period, startDate, endDate);
    }
    
    private PlatformOverview createNewOverview(Period period, LocalDate startDate, LocalDate endDate) {
        Count totalUsers = aggregationService.getTotalUsersAtDate(endDate);
        Count newUsersCount = aggregationService.getNewUsersInPeriod(startDate, endDate);
        
        Count totalActiveCourses = Count.zero();
        Count newCoursesCount = Count.zero();
        Count totalEnrollments = Count.zero();
        Count newEnrollmentsCount = Count.zero();
        Money totalRevenue = Money.zero();
        Percentage averageCompletionRate = Percentage.zero();
        
        Optional<PlatformOverview> previousPeriod = repository
            .findPreviousPeriod(period, endDate);
        
        Percentage userGrowthRate = calculationService.calculateGrowthRate(
            previousPeriod.map(PlatformOverview::getTotalUsers).orElse(Count.zero()),
            totalUsers
        );
        
        Percentage revenueGrowthRate = calculationService.calculateRevenueGrowthRate(
            previousPeriod.map(PlatformOverview::getTotalRevenue).orElse(Money.zero()),
            totalRevenue
        );
        
        Percentage enrollmentGrowthRate = calculationService.calculateGrowthRate(
            previousPeriod.map(PlatformOverview::getTotalEnrollments).orElse(Count.zero()),
            totalEnrollments
        );
        
        return PlatformOverview.create(
            period, startDate, endDate,
            totalUsers, totalActiveCourses, Count.zero(),
            totalEnrollments, totalRevenue, averageCompletionRate,
            newUsersCount, newCoursesCount, newEnrollmentsCount,
            userGrowthRate, revenueGrowthRate, enrollmentGrowthRate
        );
    }
    
    private void updateOverviewMetrics(PlatformOverview overview, LocalDate startDate, LocalDate endDate) {
        Count totalUsers = aggregationService.getTotalUsersAtDate(endDate);
        Count newUsersCount = aggregationService.getNewUsersInPeriod(startDate, endDate);
        
        Count totalActiveCourses = Count.zero();
        Count newCoursesCount = Count.zero();
        Count totalEnrollments = Count.zero();
        Count newEnrollmentsCount = Count.zero();
        Money totalRevenue = Money.zero();
        Percentage averageCompletionRate = Percentage.zero();
        
        Optional<PlatformOverview> previousPeriod = repository
            .findPreviousPeriod(overview.getPeriod(), endDate);
        
        Percentage userGrowthRate = calculationService.calculateGrowthRate(
            previousPeriod.map(PlatformOverview::getTotalUsers).orElse(Count.zero()),
            totalUsers
        );
        
        Percentage revenueGrowthRate = calculationService.calculateRevenueGrowthRate(
            previousPeriod.map(PlatformOverview::getTotalRevenue).orElse(Money.zero()),
            totalRevenue
        );
        
        Percentage enrollmentGrowthRate = calculationService.calculateGrowthRate(
            previousPeriod.map(PlatformOverview::getTotalEnrollments).orElse(Count.zero()),
            totalEnrollments
        );
        
        overview.updateMetrics(
            totalUsers, totalActiveCourses, Count.zero(),
            totalEnrollments, totalRevenue, averageCompletionRate,
            newUsersCount, newCoursesCount, newEnrollmentsCount,
            userGrowthRate, revenueGrowthRate, enrollmentGrowthRate
        );
    }
    
    private LocalDate[] calculatePeriodDates(Period period) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        
        switch (period) {
            case DAILY:
                startDate = now;
                endDate = now;
                break;
                
            case WEEKLY:
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                endDate = startDate.plusDays(6);
                break;
                
            case MONTHLY:
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;
                
            case YEARLY:
                startDate = now.withDayOfYear(1);
                endDate = now.withDayOfYear(now.lengthOfYear());
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
        
        return new LocalDate[]{startDate, endDate};
    }
}

