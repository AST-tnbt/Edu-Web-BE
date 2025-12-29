package com.se347.analysticservice.entities.shared.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Count Value Object representing a non-negative counter.
 * Encapsulates Long with domain-specific validation and operations.
 * 
 * This is an immutable value object following DDD principles.
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // For factory methods
@Getter
public class Count {
    
    @Column
    private Long value;
    
    // ==================== Factory Methods ====================
    
    /**
     * Creates Count from Long value.
     * @throws IllegalArgumentException if value is null or negative
     */
    public static Count of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("Count value cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + value);
        }
        return new Count(value);
    }
    
    /**
     * Creates Count from int value.
     */
    public static Count of(int value) {
        return of((long) value);
    }
    
    /**
     * Creates zero count.
     */
    public static Count zero() {
        return new Count(0L);
    }
    
    /**
     * Creates count of one.
     */
    public static Count one() {
        return new Count(1L);
    }
    
    // ==================== Arithmetic Operations ====================
    
    /**
     * Increments count by 1.
     * @return new Count instance
     */
    public Count increment() {
        return new Count(this.value + 1);
    }
    
    /**
     * Decrements count by 1.
     * @return new Count instance
     * @throws IllegalArgumentException if result would be negative
     */
    public Count decrement() {
        if (this.value == 0) {
            throw new IllegalArgumentException("Cannot decrement count below zero");
        }
        return new Count(this.value - 1);
    }
    
    /**
     * Adds another count.
     * @return new Count instance
     */
    public Count add(Count other) {
        if (other == null) {
            return this;
        }
        return new Count(this.value + other.value);
    }
    
    /**
     * Adds a long value.
     */
    public Count add(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Cannot add negative value: " + value);
        }
        return new Count(this.value + value);
    }
    
    /**
     * Subtracts another count.
     * @return new Count instance
     * @throws IllegalArgumentException if result would be negative
     */
    public Count subtract(Count other) {
        if (other == null) {
            return this;
        }
        if (this.value < other.value) {
            throw new IllegalArgumentException("Subtraction would result in negative count");
        }
        return new Count(this.value - other.value);
    }
    
    /**
     * Subtracts a long value.
     */
    public Count subtract(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Cannot subtract negative value: " + value);
        }
        if (this.value < value) {
            throw new IllegalArgumentException("Subtraction would result in negative count");
        }
        return new Count(this.value - value);
    }
    
    /**
     * Multiplies by a factor.
     * @return new Count instance
     */
    public Count multiply(long factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor cannot be negative: " + factor);
        }
        return new Count(this.value * factor);
    }
    
    /**
     * Divides by a divisor.
     * @return new Count instance
     * @throws ArithmeticException if divisor is zero
     */
    public Count divide(long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        if (divisor < 0) {
            throw new IllegalArgumentException("Divisor cannot be negative: " + divisor);
        }
        return new Count(this.value / divisor);
    }
    
    // ==================== Comparison Operations ====================
    
    /**
     * Checks if this count is greater than another.
     */
    public boolean isGreaterThan(Count other) {
        return this.value > other.value;
    }
    
    /**
     * Checks if this count is greater than a long value.
     */
    public boolean isGreaterThan(long other) {
        return this.value > other;
    }
    
    /**
     * Checks if this count is greater than or equal to another.
     */
    public boolean isGreaterThanOrEqual(Count other) {
        return this.value >= other.value;
    }
    
    /**
     * Checks if this count is less than another.
     */
    public boolean isLessThan(Count other) {
        return this.value < other.value;
    }
    
    /**
     * Checks if this count is less than a long value.
     */
    public boolean isLessThan(long other) {
        return this.value < other;
    }
    
    /**
     * Checks if this count is less than or equal to another.
     */
    public boolean isLessThanOrEqual(Count other) {
        return this.value <= other.value;
    }
    
    /**
     * Checks if count is zero.
     */
    public boolean isZero() {
        return this.value == 0;
    }
    
    /**
     * Checks if count is positive (> 0).
     */
    public boolean isPositive() {
        return this.value > 0;
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Returns the larger of two counts.
     */
    public Count max(Count other) {
        return isGreaterThan(other) ? this : other;
    }
    
    /**
     * Returns the smaller of two counts.
     */
    public Count min(Count other) {
        return isLessThan(other) ? this : other;
    }
    
    /**
     * Calculates percentage this count represents of a total.
     * Example: Count.of(20).percentageOf(Count.of(100)) = Percentage.of(20)
     */
    public Percentage percentageOf(Count total) {
        if (total.isZero()) {
            return Percentage.zero();
        }
        return Percentage.between(this.value, total.value);
    }
    
    /**
     * Calculates percentage this count represents of a total long value.
     */
    public Percentage percentageOf(long total) {
        if (total == 0) {
            return Percentage.zero();
        }
        return Percentage.between(this.value, total);
    }
    
    /**
     * Formats count as string with thousand separators.
     * Example: "1,234"
     */
    public String formatted() {
        return String.format("%,d", value);
    }
    
    /**
     * Returns value as int (may overflow for large counts).
     * Use with caution.
     */
    public int toInt() {
        if (value > Integer.MAX_VALUE) {
            throw new ArithmeticException("Count value exceeds Integer.MAX_VALUE");
        }
        return value.intValue();
    }
    
    /**
     * Returns value as double (for calculations).
     */
    public double toDouble() {
        return value.doubleValue();
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Count count = (Count) o;
        return Objects.equals(value, count.value);
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

