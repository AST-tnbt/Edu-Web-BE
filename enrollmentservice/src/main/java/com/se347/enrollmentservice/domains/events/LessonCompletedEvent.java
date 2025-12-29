package com.se347.enrollmentservice.domains.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: Student completed a lesson
 * 
 * WHEN RAISED:
 * - Student marks a lesson as completed
 * 
 * WHO LISTENS:
 * - AnalyticsService: Track learning progress
 * - GamificationService: Award points/badges
 * - NotificationService: Congratulate student
 */
@Getter
@RequiredArgsConstructor
public class LessonCompletedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final UUID enrollmentId;
    private final UUID courseId;
    private final UUID studentId;
    private final UUID lessonId;
    private final int lessonsCompleted;
    private final int totalLessons;
    private final double overallProgress;
    private final LocalDateTime occurredAt;
    
    public static LessonCompletedEvent now(
        UUID enrollmentId,
        UUID courseId,
        UUID studentId,
        UUID lessonId,
        int lessonsCompleted,
        int totalLessons,
        double overallProgress
    ) {
        return new LessonCompletedEvent(
            UUID.randomUUID(),
            enrollmentId,
            courseId,
            studentId,
            lessonId,
            lessonsCompleted,
            totalLessons,
            overallProgress,
            LocalDateTime.now()
        );
    }
}

