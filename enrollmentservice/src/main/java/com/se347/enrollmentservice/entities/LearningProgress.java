package com.se347.enrollmentservice.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "learning_progress", 
       uniqueConstraints = @UniqueConstraint(name = "uk_lesson_enrollment", columnNames = {"lessonId", "enrollmentId"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID learningProgressId;

    @Column(nullable = false)
    private UUID enrollmentId;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false)
    private boolean isCompleted;

    @Column(updatable = true)
    private LocalDateTime lastAccessedAt;

    @Column(updatable = true)
    private LocalDateTime completedAt;
}
