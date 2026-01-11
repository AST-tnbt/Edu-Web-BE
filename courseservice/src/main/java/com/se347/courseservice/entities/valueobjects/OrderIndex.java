package com.se347.courseservice.entities.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.Column;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class OrderIndex {
    
    @Column(name = "order_index", nullable = false)
    private int value;
    
    /**
     * Factory method - tạo OrderIndex từ int
     */
    public static OrderIndex of(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Order index must be non-negative, got: " + value);
        }
        return new OrderIndex(value);
    }
    
    /**
     * Factory method - tạo OrderIndex = 0
     */
    public static OrderIndex zero() {
        return new OrderIndex(0);
    }
    
    /**
     * Increment order index by 1
     */
    public OrderIndex increment() {
        return new OrderIndex(this.value + 1);
    }
    
    /**
     * Check if this order index is before another
     */
    public boolean isBefore(OrderIndex other) {
        return this.value < other.value;
    }
    
    /**
     * Check if this order index is after another
     */
    public boolean isAfter(OrderIndex other) {
        return this.value > other.value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderIndex that = (OrderIndex) o;
        return value == that.value;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
    
    @Override
    public String toString() {
        return "OrderIndex(" + value + ")";
    }
}