package com.se347.analysticservice.domains.services.shared;

import com.se347.analysticservice.entities.shared.valueobjects.Percentage;

import java.util.List;

public final class PercentageCalculationHelper {
    
    private PercentageCalculationHelper() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static Percentage calculateAverage(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return Percentage.zero();
        }
        
        double sum = values.stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        double average = sum / values.size();
        return Percentage.of(average);
    }
    
    public static Percentage calculateAverageFromPercentages(List<Percentage> percentages) {
        if (percentages == null || percentages.isEmpty()) {
            return Percentage.zero();
        }
        
        double sum = percentages.stream()
            .mapToDouble(Percentage::getValue)
            .sum();
        
        double average = sum / percentages.size();
        return Percentage.of(average);
    }
    
    public static Percentage calculateWeightedAverage(List<Double> values, List<Double> weights) {
        if (values == null || weights == null || values.isEmpty() || weights.isEmpty()) {
            return Percentage.zero();
        }
        
        if (values.size() != weights.size()) {
            throw new IllegalArgumentException("Values and weights must have the same size");
        }
        
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (int i = 0; i < values.size(); i++) {
            weightedSum += values.get(i) * weights.get(i);
            totalWeight += weights.get(i);
        }
        
        if (totalWeight == 0.0) {
            return Percentage.zero();
        }
        
        double average = weightedSum / totalWeight;
        return Percentage.of(average);
    }
}

