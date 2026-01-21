package com.se347.analysticservice.entities.admin.instructor;

import com.se347.analysticservice.entities.shared.valueobjects.Count;
import com.se347.analysticservice.entities.shared.valueobjects.Percentage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Read-only projection/DTO for admin analytics.
 * This is a lightweight projection from InstructorOverview for admin reporting purposes.
 * All business logic and data updates are handled in InstructorOverview entity.
 */
@Getter
@AllArgsConstructor
public class InstructorStats {
    
    private UUID instructorId;
    private Count totalCourses;
    private Count totalStudents;
    private Percentage averageCompletionRatePercent;

    /**
     * Creates a projection from InstructorOverview for admin reporting.
     * This is a read-only DTO, not a persistent entity.
     */
    public static InstructorStats fromInstructorOverview(
        UUID instructorId,
        Count totalCourses,
        Count totalStudents,
        Percentage averageCompletionRate
    ) {
        if (instructorId == null) throw new IllegalArgumentException("Instructor ID cannot be null");
        if (totalCourses == null) throw new IllegalArgumentException("Total courses cannot be null");
        if (totalStudents == null) throw new IllegalArgumentException("Total students cannot be null");
        if (averageCompletionRate == null) throw new IllegalArgumentException("Average completion rate cannot be null");

        return new InstructorStats(
            instructorId,
            totalCourses,
            totalStudents,
            averageCompletionRate
        );
    }

    /**
     * Backward-compatible getter to avoid breaking older code/DTOs.
     * Prefer {@link #getAverageCompletionRatePercent()} for clarity.
     */
    public Percentage getAverageCompletionRate() {
        return averageCompletionRatePercent;
    }
}

