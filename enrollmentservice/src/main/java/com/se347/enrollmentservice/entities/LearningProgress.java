package com.se347.enrollmentservice.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "learning_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID learningProgressId;


    @Column(nullable = false)
    private UUID enrollmentId;

    @Column(nullable = false)
    private UUID contentId;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false)
    private double progressPercentage;

    @Column(nullable = false)
    private int timeSpent;

    @Column(nullable = false)
    private boolean isCompleted;

    @Column(nullable = false)
    private LocalDateTime lastAccessedAt;

    @Column(nullable = false)
    private LocalDateTime completedAt;
}
