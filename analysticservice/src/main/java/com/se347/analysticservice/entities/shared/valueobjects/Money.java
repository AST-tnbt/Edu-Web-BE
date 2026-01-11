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
 * Money Value Object representing monetary amount in the system.
 * Encapsulates BigDecimal with domain-specific validation and operations.
 * 
 * This is an immutable value object following DDD principles.
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // For factory methods
@Getter
public class Money {
    
    @Column(precision = 15, scale = 2)
    private BigDecimal amount;
    
    // ==================== Factory Methods ====================
    
    /**
     * Creates Money from BigDecimal amount.
     * @throws IllegalArgumentException if amount is null or negative
     */
    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        return new Money(amount.setScale(2, RoundingMode.HALF_UP));
    }
    
    /**
     * Creates Money from double value.
     */
    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }
    
    /**
     * Creates Money from long value.
     */
    public static Money of(long amount) {
        return of(BigDecimal.valueOf(amount));
    }
    
    /**
     * Creates zero money.
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
    
    // ==================== Arithmetic Operations ====================
    
    /**
     * Adds another money amount.
     * @return new Money instance with sum
     */
    public Money add(Money other) {
        if (other == null) {
            return this;
        }
        return new Money(this.amount.add(other.amount));
    }
    
    /**
     * Subtracts another money amount.
     * @return new Money instance with difference
     * @throws IllegalArgumentException if result would be negative
     */
    public Money subtract(Money other) {
        if (other == null) {
            return this;
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtraction result cannot be negative");
        }
        return new Money(result);
    }
    
    /**
     * Multiplies by a factor.
     * @return new Money instance
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null || factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Factor must be non-negative");
        }
        return new Money(this.amount.multiply(factor).setScale(2, RoundingMode.HALF_UP));
    }
    
    /**
     * Multiplies by a double factor.
     */
    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }
    
    /**
     * Divides by a divisor.
     * @return new Money instance
     * @throws ArithmeticException if divisor is zero
     */
    public Money divide(BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return new Money(this.amount.divide(divisor, 2, RoundingMode.HALF_UP));
    }
    
    /**
     * Divides by a long divisor.
     */
    public Money divide(long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return divide(BigDecimal.valueOf(divisor));
    }
    
    /**
     * Calculates percentage of this amount.
     * Example: Money.of(100).percentage(15) = Money.of(15)
     */
    public Money percentage(double percent) {
        return multiply(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
    }
    
    // ==================== Comparison Operations ====================
    
    /**
     * Checks if this money is greater than another.
     */
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Checks if this money is greater than or equal to another.
     */
    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    /**
     * Checks if this money is less than another.
     */
    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }
    
    /**
     * Checks if this money is less than or equal to another.
     */
    public boolean isLessThanOrEqual(Money other) {
        return this.amount.compareTo(other.amount) <= 0;
    }
    
    /**
     * Checks if amount is zero.
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Checks if amount is positive (> 0).
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Returns the larger of two money amounts.
     */
    public Money max(Money other) {
        return isGreaterThan(other) ? this : other;
    }
    
    /**
     * Returns the smaller of two money amounts.
     */
    public Money min(Money other) {
        return isLessThan(other) ? this : other;
    }
    
    /**
     * Formats money as string with currency.
     * Example: "$1,234.56"
     */
    public String formatted() {
        return String.format("$%,.2f", amount);
    }
    
    /**
     * Returns amount as double (for calculations only).
     * Use with caution due to precision loss.
     */
    public double toDouble() {
        return amount.doubleValue();
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
    
    @Override
    public String toString() {
        return formatted();
    }
}

