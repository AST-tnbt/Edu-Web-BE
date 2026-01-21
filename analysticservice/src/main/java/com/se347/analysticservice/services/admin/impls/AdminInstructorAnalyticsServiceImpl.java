package com.se347.analysticservice.services.admin.impls;

import com.se347.analysticservice.entities.instructor.InstructorOverview;
import com.se347.analysticservice.repositories.InstructorOverviewRepository;
import com.se347.analysticservice.services.admin.AdminInstructorAnalyticsService;

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
public class AdminInstructorAnalyticsServiceImpl implements AdminInstructorAnalyticsService {
    
    private final InstructorOverviewRepository overviewRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InstructorOverview> getInstructorStats(UUID instructorId) {
        return overviewRepository.findByInstructorId(instructorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorOverview> getTopInstructorsByStudents(int limit) {
        return overviewRepository.findAllOrderByTotalStudentsDesc()
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorOverview> getTopInstructorsByCourses(int limit) {
        return overviewRepository.findAllOrderByTotalCoursesDesc()
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InstructorOverview> getTopInstructorsByRevenue(int limit) {
        return overviewRepository.findAllOrderByTotalRevenueDesc()
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
}

