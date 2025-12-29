package com.se347.analysticservice.entities.events.revenue;

import com.se347.analysticservice.entities.events.DomainEvent;
import com.se347.analysticservice.enums.Period;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when instructor revenue for a period is calculated.
 * Simplified: Only tracks total revenue, no commission/fee.
 */
@Value
public class InstructorRevenueCalculatedEvent implements DomainEvent {
    
    UUID eventId;
    UUID instructorRevenueId;
    UUID instructorId;
    Period period;
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal totalRevenue;
    Long totalEnrollments;
    Long totalCourses;
    LocalDateTime occurredAt;
    
    public static InstructorRevenueCalculatedEvent now(
        UUID instructorRevenueId,
        UUID instructorId,
        Period period,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalRevenue,
        Long totalEnrollments,
        Long totalCourses
    ) {
        return new InstructorRevenueCalculatedEvent(
            UUID.randomUUID(),
            instructorRevenueId,
            instructorId,
            period,
            startDate,
            endDate,
            totalRevenue,
            totalEnrollments,
            totalCourses,
            LocalDateTime.now()
        );
    }
}
