package com.se347.analysticservice.services.instructor.impls;

import com.se347.analysticservice.entities.instructor.InstructorDailyStats;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.repositories.InstructorDailyStatsRepository;
import com.se347.analysticservice.services.instructor.InstructorDailyStatsService;

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
public class InstructorDailyStatsServiceImpl implements InstructorDailyStatsService {
    
    private final InstructorDailyStatsRepository dailyStatsRepository;
    
    @Override
    @Transactional
    public void recordEnrollment(UUID instructorId, LocalDate date) {
        log.debug("Recording enrollment for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = getOrCreate(instructorId, date);
        
        dailyStats.recordEnrollment();
        dailyStatsRepository.save(dailyStats);
    }
    
    @Override
    @Transactional
    public void recordActiveStudent(UUID instructorId, LocalDate date) {
        log.debug("Recording active student for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = getOrCreate(instructorId, date);
        
        dailyStats.recordActiveStudent();
        dailyStatsRepository.save(dailyStats);
    }
    
    @Override
    @Transactional
    public void recordRevenue(UUID instructorId, LocalDate date, Money amount) {
        log.debug("Recording revenue for daily stats: instructorId={}, date={}, amount={}", 
            instructorId, date, amount.getAmount());
        
        InstructorDailyStats dailyStats = getOrCreate(instructorId, date);
        
        dailyStats.recordRevenue(amount);
        dailyStatsRepository.save(dailyStats);
    }
    
    @Override
    @Transactional
    public void recordCourseCompletion(UUID instructorId, LocalDate date) {
        log.debug("Recording course completion for daily stats: instructorId={}, date={}", instructorId, date);
        
        InstructorDailyStats dailyStats = getOrCreate(instructorId, date);
        
        dailyStats.recordCourseCompletion();
        dailyStatsRepository.save(dailyStats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorDailyStats> getByInstructorIdAndDate(UUID instructorId, LocalDate date) {
        return dailyStatsRepository.findByInstructorIdAndDate(instructorId, date);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorDailyStats> getByInstructorId(UUID instructorId) {
        return dailyStatsRepository.findByInstructorId(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorDailyStats> getByInstructorIdAndDateBetween(UUID instructorId, LocalDate startDate, LocalDate endDate) {
        return dailyStatsRepository.findByInstructorIdAndDateBetween(instructorId, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorDailyStats> getByInstructorIdOrderByDateDesc(UUID instructorId) {
        return dailyStatsRepository.findByInstructorIdOrderByDateDesc(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public InstructorDailyStats getOrCreate(UUID instructorId, LocalDate date) {
        return dailyStatsRepository.findByInstructorIdAndDate(instructorId, date)
            .orElseGet(() -> createInitialDailyStats(instructorId, date));
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
