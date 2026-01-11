package com.se347.courseservice.entities.valueobjects;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Money {
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal amount;
    
    // Factory method
    public static Money of(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        return new Money(amount);
    }
    
    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }
    
    // Domain methods
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
