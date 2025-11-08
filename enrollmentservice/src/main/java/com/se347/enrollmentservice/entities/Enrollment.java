package com.se347.enrollmentservice.entities;

import com.se347.enrollmentservice.enums.EnrollmentStatus;
import com.se347.enrollmentservice.enums.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID enrollmentId;

    @Column(nullable = false)
    private UUID courseId;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    @Column(nullable = false)
    private EnrollmentStatus enrollmentStatus;

    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = true)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
