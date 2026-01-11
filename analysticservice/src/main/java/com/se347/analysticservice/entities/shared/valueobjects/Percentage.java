package com.se347.analysticservice.entities.shared.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Percentage Value Object representing a percentage value.
 * Can represent both regular percentages (0-100%) and growth rates (can be negative or > 100%).
 * 
 * This is an immutable value object following DDD principles.
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // For factory methods
@Getter
public class Percentage {
    
    @Column(precision = 10, scale = 4)
    private Double value; // Stored as 0-100 (e.g., 15.5 means 15.5%)
    
    // ==================== Factory Methods ====================
    
    /**
     * Creates Percentage from double value (0-100).
     * @param value percentage value (e.g., 15.5 for 15.5%)
     */
    public static Percentage of(Double value) {
        if (value == null) {
            throw new IllegalArgumentException("Percentage value cannot be null");
        }
        return new Percentage(round(value));
    }
    
    /**
     * Creates Percentage from fraction (0-1).
     * Example: fromFraction(0.155) = 15.5%
     */
    public static Percentage fromFraction(Double fraction) {
        if (fraction == null) {
            throw new IllegalArgumentException("Fraction cannot be null");
        }
        return new Percentage(round(fraction * 100));
    }
    
    /**
     * Creates zero percentage (0%).
     */
    public static Percentage zero() {
        return new Percentage(0.0);
    }
    
    /**
     * Creates 100 percentage (100%).
     */
    public static Percentage hundred() {
        return new Percentage(100.0);
    }
    
    /**
     * Calculates percentage between two values.
     * Example: between(20, 100) = 20%
     */
    public static Percentage between(long part, long total) {
        if (total == 0) {
            return zero();
        }
        return of((double) part / total * 100);
    }
    
    /**
     * Calculates growth rate between old and new values.
     * Example: growthRate(100, 120) = 20% (increase)
     *          growthRate(100, 80) = -20% (decrease)
     */
    public static Percentage growthRate(Number oldValue, Number newValue) {
        if (oldValue == null || newValue == null) {
            throw new IllegalArgumentException("Values cannot be null");
        }
        double old = oldValue.doubleValue();
        double newVal = newValue.doubleValue();
        
        if (old == 0) {
            return newVal > 0 ? hundred() : zero();
        }
        
        double rate = ((newVal - old) / old) * 100;
        return of(rate);
    }
    
    // ==================== Validation ====================
    
    /**
     * Creates Percentage with validation for regular percentage (0-100%).
     * Use this for completion rates, success rates, etc.
     */
    public static Percentage ofBounded(Double value) {
        if (value == null) {
            throw new IllegalArgumentException("Percentage value cannot be null");
        }
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100: " + value);
        }
        return new Percentage(round(value));
    }
    
    // ==================== Arithmetic Operations ====================
    
    /**
     * Adds another percentage.
     * Example: 15% + 10% = 25%
     */
    public Percentage add(Percentage other) {
        if (other == null) {
            return this;
        }
        return new Percentage(round(this.value + other.value));
    }
    
    /**
     * Subtracts another percentage.
     * Example: 15% - 10% = 5%
     */
    public Percentage subtract(Percentage other) {
        if (other == null) {
            return this;
        }
        return new Percentage(round(this.value - other.value));
    }
    
    /**
     * Multiplies by a factor.
     */
    public Percentage multiply(double factor) {
        return new Percentage(round(this.value * factor));
    }
    
    /**
     * Divides by a divisor.
     */
    public Percentage divide(double divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new Percentage(round(this.value / divisor));
    }
    
    // ==================== Comparison Operations ====================
    
    /**
     * Checks if this percentage is greater than another.
     */
    public boolean isGreaterThan(Percentage other) {
        return this.value > other.value;
    }
    
    /**
     * Checks if this percentage is less than another.
     */
    public boolean isLessThan(Percentage other) {
        return this.value < other.value;
    }
    
    /**
     * Checks if value is zero.
     */
    public boolean isZero() {
        return Math.abs(this.value) < 0.0001; // Consider floating point precision
    }
    
    /**
     * Checks if value is positive (> 0).
     */
    public boolean isPositive() {
        return this.value > 0;
    }
    
    /**
     * Checks if value is negative (< 0).
     * Useful for checking if growth rate is negative.
     */
    public boolean isNegative() {
        return this.value < 0;
    }
    
    /**
     * Checks if this is a growth (positive change).
     */
    public boolean isPositiveGrowth() {
        return isPositive();
    }
    
    /**
     * Checks if this is a decline (negative change).
     */
    public boolean isDecline() {
        return isNegative();
    }
    
    // ==================== Application Methods ====================
    
    /**
     * Applies this percentage to a value.
     * Example: Percentage.of(20).applyTo(100) = 20
     */
    public double applyTo(double baseValue) {
        return baseValue * (this.value / 100);
    }
    
    /**
     * Applies this percentage to Money.
     * Example: Percentage.of(20).applyTo(Money.of(100)) = Money.of(20)
     */
    public Money applyTo(Money money) {
        return money.percentage(this.value);
    }
    
    /**
     * Returns fraction representation (0-1).
     * Example: 15.5% returns 0.155
     */
    public double asFraction() {
        return this.value / 100;
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Formats as string with % symbol.
     * Example: "15.50%"
     */
    public String formatted() {
        return String.format("%.2f%%", value);
    }
    
    /**
     * Formats as string with + for positive values.
     * Example: "+15.50%" or "-10.00%"
     * Useful for displaying growth rates.
     */
    public String formattedWithSign() {
        return String.format("%+.2f%%", value);
    }
    
    /**
     * Returns absolute value of percentage.
     */
    public Percentage abs() {
        return new Percentage(Math.abs(this.value));
    }
    
    /**
     * Rounds percentage value to 2 decimal places.
     */
    private static Double round(Double value) {
        return BigDecimal.valueOf(value)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Percentage that = (Percentage) o;
        return Math.abs(value - that.value) < 0.0001; // Consider floating point precision
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return formatted();
    }
}

