package com.se347.enrollmentservice.entities.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object for percentage values (0-100%)
 * 
 * DDD PRINCIPLES:
 * - Immutable (no setters)
 * - Self-validating (ensures 0 <= value <= 100)
 * - Domain operations (add, isComplete, etc.)
 * 
 * WHY VALUE OBJECT?
 * - Encapsulates percentage validation
 * - Provides domain-specific operations
 * - Type-safe (can't confuse with raw double)
 */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class Percentage {
    
    private double value; // Stored as 0-100
    
    private Percentage(double value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(
                "Percentage must be between 0 and 100, got: " + value
            );
        }
        // Round to 2 decimal places
        this.value = BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }
    
    // ========== FACTORY METHODS ==========
    
    /**
     * Create percentage from double value (0-100)
     */
    public static Percentage of(double value) {
        return new Percentage(value);
    }
    
    /**
     * Create percentage from fraction (completed/total)
     */
    public static Percentage fromFraction(int completed, int total) {
        if (total <= 0) {
            return zero();
        }
        if (completed < 0) {
            throw new IllegalArgumentException("Completed count cannot be negative: " + completed);
        }
        double percentage = ((double) completed / total) * 100.0;
        return new Percentage(percentage);
    }
    
    /**
     * Zero percentage (0%)
     */
    public static Percentage zero() {
        return new Percentage(0.0);
    }
    
    /**
     * Complete percentage (100%)
     */
    public static Percentage complete() {
        return new Percentage(100.0);
    }
    
    // ========== DOMAIN OPERATIONS ==========
    
    /**
     * Check if percentage is complete (100%)
     */
    public boolean isComplete() {
        return value >= 100.0;
    }
    
    /**
     * Check if percentage is zero (0%)
     */
    public boolean isZero() {
        return value == 0.0;
    }
    
    /**
     * Check if this percentage is greater than another
     */
    public boolean isGreaterThan(Percentage other) {
        return this.value > other.value;
    }
    
    /**
     * Get formatted string (e.g., "75.50%")
     */
    public String formatted() {
        return String.format("%.2f%%", value);
    }
    
    @Override
    public String toString() {
        return formatted();
    }
}

