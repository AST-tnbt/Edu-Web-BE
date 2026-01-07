package com.se347.analysticservice.domains.services.instructor;

import com.se347.analysticservice.entities.admin.instructor.InstructorStats;
import com.se347.analysticservice.entities.admin.revenue.InstructorRevenue;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import org.springframework.stereotype.Service;

@Service
public class InstructorPerformanceService {
    
    public double calculateStudentsPerCourse(InstructorStats stats) {
        if (stats == null) {
            throw new IllegalArgumentException("Stats cannot be null");
        }
        
        if (stats.getTotalCourses().isZero()) {
            return 0.0;
        }
        
        return stats.getTotalStudents().toDouble() / stats.getTotalCourses().toDouble();
    }
    
    public Money calculateRevenuePerStudent(InstructorRevenue revenue) {
        if (revenue == null) {
            throw new IllegalArgumentException("Revenue cannot be null");
        }
        
        if (revenue.getTotalEnrollments().isZero()) {
            return Money.zero();
        }
        
        return revenue.getTotalRevenue().divide(revenue.getTotalEnrollments().getValue());
    }
    
    public Money calculateRevenuePerCourse(InstructorRevenue revenue) {
        if (revenue == null) {
            throw new IllegalArgumentException("Revenue cannot be null");
        }
        
        if (revenue.getTotalCourses().isZero()) {
            return Money.zero();
        }
        
        return revenue.getTotalRevenue().divide(revenue.getTotalCourses().getValue());
    }
    
    public boolean isTopPerformer(Money instructorRevenue, Money averageRevenue, Percentage threshold) {
        if (instructorRevenue == null || averageRevenue == null || threshold == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        if (averageRevenue.isZero()) {
            return instructorRevenue.isPositive();
        }
        
        double performanceRatio = instructorRevenue.getAmount().doubleValue() / 
                                 averageRevenue.getAmount().doubleValue();
        
        return performanceRatio >= (1 + threshold.getValue() / 100);
    }
    
    public Percentage calculateEnrollmentGrowth(Count previousEnrollments, Count currentEnrollments) {
        if (previousEnrollments == null || currentEnrollments == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        if (previousEnrollments.isZero()) {
            return currentEnrollments.isPositive() ? Percentage.of(100.0) : Percentage.zero();
        }
        
        double growth = ((currentEnrollments.getValue() - previousEnrollments.getValue()) * 100.0) 
                       / previousEnrollments.getValue();
        
        return Percentage.of(growth);
    }
    
    public int calculatePerformanceScore(InstructorRevenue revenue, Money averageRevenue) {
        if (revenue == null || averageRevenue == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        int score = 0;
        
        if (!revenue.getTotalCourses().isZero()) {
            score += 20;
        }
        
        if (revenue.getTotalEnrollments().isGreaterThan(Count.of(10L))) {
            score += 30;
        }
        
        if (!averageRevenue.isZero() && 
            revenue.getTotalRevenue().isGreaterThan(averageRevenue)) {
            score += 50;
        }
        
        return score;
    }
}

