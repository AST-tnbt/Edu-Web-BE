package com.se347.enrollmentservice.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID courseProgressId;

    @Column(nullable = false)
    private UUID enrollmentId;

    @Column(nullable = false)
    private double overallProgress;

    @Column(nullable = false)
    private int lessonsCompleted;

    @Column(nullable = false)
    private int totalLessons;

    @Column(nullable = false)
    private boolean isCourseCompleted;

    @Column(nullable = false)
    private LocalDateTime courseCompletedAt;

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
