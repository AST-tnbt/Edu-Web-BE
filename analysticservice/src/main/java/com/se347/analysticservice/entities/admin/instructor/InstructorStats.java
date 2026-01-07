package com.se347.analysticservice.entities.admin.instructor;

import com.se347.analysticservice.domains.events.instructor.InstructorStatsCreatedEvent;
import com.se347.analysticservice.domains.events.instructor.InstructorStatsUpdatedEvent;
import com.se347.analysticservice.entities.AbstractAggregateRoot;
import com.se347.analysticservice.entities.shared.valueobjects.Count;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instructor_stats", indexes = {
    @Index(name = "idx_instructor_id", columnList = "instructor_id"),
    @Index(name = "idx_total_students", columnList = "total_students DESC"),
    @Index(name = "idx_total_courses", columnList = "total_courses DESC")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_instructor_id", columnNames = "instructor_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstructorStats extends AbstractAggregateRoot<InstructorStats> {
    
    @Id
    private UUID instructorStatsId;
    
    @Column(nullable = false)
    private UUID instructorId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_courses", nullable = false))
    private Count totalCourses;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_students", nullable = false))
    private Count totalStudents;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static InstructorStats create(UUID instructorId, Count totalCourses, Count totalStudents) {
        if (instructorId == null) throw new IllegalArgumentException("Instructor ID cannot be null");
        if (totalCourses == null) throw new IllegalArgumentException("Total courses cannot be null");
        if (totalStudents == null) throw new IllegalArgumentException("Total students cannot be null");

        InstructorStats instructorStats = new InstructorStats();
        instructorStats.instructorStatsId = UUID.randomUUID(); // Generate ID immediately
        instructorStats.instructorId = instructorId;
        instructorStats.totalCourses = totalCourses;
        instructorStats.totalStudents = totalStudents;
        instructorStats.onCreate();
        
        // Register domain event (now ID is available)
        instructorStats.registerEvent(
            InstructorStatsCreatedEvent.now(
                instructorStats.instructorStatsId,
                instructorId,
                totalCourses.getValue(),
                totalStudents.getValue()
            )
        );
        
        return instructorStats;
    }

    public void updateMetrics(Count totalCourses, Count totalStudents) {
        if (totalCourses == null) throw new IllegalArgumentException("Total courses cannot be null");
        if (totalStudents == null) throw new IllegalArgumentException("Total students cannot be null");

        this.totalCourses = totalCourses;
        this.totalStudents = totalStudents;
        this.onUpdate();
        
        // Register domain event
        this.registerEvent(
            InstructorStatsUpdatedEvent.now(
                this.instructorStatsId,
                this.instructorId,
                totalCourses.getValue(),
                totalStudents.getValue()
            )
        );
    }
}

