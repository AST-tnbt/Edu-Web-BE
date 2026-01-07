package com.se347.analysticservice.domains.services.platform;

import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import org.springframework.stereotype.Service;

@Service
public class PlatformMetricsCalculationService {
    
    public Percentage calculateGrowthRate(Count previous, Count current) {
        if (previous == null || current == null) {
            throw new IllegalArgumentException("Counts cannot be null");
        }
        
        if (previous.isZero() && current.isZero()) {
            return Percentage.zero();
        }
        
        if (previous.isZero()) {
            return Percentage.of(100.0);
        }
        
        double rate = ((current.getValue() - previous.getValue()) * 100.0) / previous.getValue();
        return Percentage.of(rate);
    }
    
    public Percentage calculateRevenueGrowthRate(Money previous, Money current) {
        if (previous == null || current == null) {
            throw new IllegalArgumentException("Money amounts cannot be null");
        }
        
        if (previous.isZero() && current.isZero()) {
            return Percentage.zero();
        }
        
        if (previous.isZero()) {
            return Percentage.of(100.0);
        }
        
        double prevAmount = previous.getAmount().doubleValue();
        double currAmount = current.getAmount().doubleValue();
        double rate = ((currAmount - prevAmount) * 100.0) / prevAmount;
        
        return Percentage.of(rate);
    }
    
    public Money calculateAverageRevenuePerUser(Money totalRevenue, Count totalUsers) {
        if (totalRevenue == null || totalUsers == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        if (totalUsers.isZero()) {
            return Money.zero();
        }
        
        return totalRevenue.divide(totalUsers.getValue());
    }
    
    public double calculateEnrollmentRate(Count totalEnrollments, Count totalUsers) {
        if (totalEnrollments == null || totalUsers == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        if (totalUsers.isZero()) {
            return 0.0;
        }
        
        return totalEnrollments.toDouble() / totalUsers.toDouble();
    }
    
    public boolean isPlatformHealthy(Percentage userGrowth, Percentage revenueGrowth, Percentage avgCompletion) {
        if (userGrowth == null || revenueGrowth == null || avgCompletion == null) {
            return false;
        }
        
        return userGrowth.isPositive() 
            && revenueGrowth.isPositive() 
            && avgCompletion.isGreaterThan(Percentage.of(50.0));
    }
}
