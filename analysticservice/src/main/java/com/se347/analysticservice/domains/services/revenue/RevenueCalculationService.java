package com.se347.analysticservice.domains.services.revenue;

import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Money;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RevenueCalculationService {
    
    public Money calculatePlatformFee(Money totalAmount, Percentage feePercentage) {
        if (totalAmount == null || feePercentage == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        return Money.of(
            totalAmount.getAmount()
                .multiply(BigDecimal.valueOf(feePercentage.getValue()))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
        );
    }
    
    public Money calculateInstructorEarning(Money totalAmount, Money platformFee) {
        if (totalAmount == null || platformFee == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        return totalAmount.subtract(platformFee);
    }
    
    public Money calculateAverageTransactionValue(Money totalRevenue, Count totalTransactions) {
        if (totalRevenue == null || totalTransactions == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        if (totalTransactions.isZero()) {
            return Money.zero();
        }
        
        return totalRevenue.divide(totalTransactions.getValue());
    }
    
    public Percentage calculateRevenueShare(Money instructorRevenue, Money totalRevenue) {
        if (instructorRevenue == null || totalRevenue == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        if (totalRevenue.isZero()) {
            return Percentage.zero();
        }
        
        double share = (instructorRevenue.getAmount().doubleValue() / 
                       totalRevenue.getAmount().doubleValue()) * 100;
        
        return Percentage.of(share);
    }
    
    public boolean meetsMinimumPayout(Money earnings, Money minimumThreshold) {
        if (earnings == null || minimumThreshold == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        return earnings.isGreaterThanOrEqual(minimumThreshold);
    }
    
    public Money calculateProjectedRevenue(Money currentRevenue, Percentage growthRate, int periods) {
        if (currentRevenue == null || growthRate == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        if (periods <= 0) {
            return currentRevenue;
        }
        
        double multiplier = Math.pow(1 + (growthRate.getValue() / 100), periods);
        return currentRevenue.multiply(multiplier);
    }
}

