package com.se347.courseservice.entities;

import com.se347.courseservice.entities.valueobjects.OrderIndex;
import com.se347.courseservice.exceptions.CourseException.*;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Section {
    
    @Id
    private UUID sectionId;
    
    @Column(name = "section_slug", unique = true, nullable = false)
    private String sectionSlug;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "order_index", nullable = false))
    })
    private OrderIndex orderIndex;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // ========== FACTORY METHOD (PACKAGE-PRIVATE!) ==========
    
    /**
     * Create new section (can only be called from Course aggregate)
     * Package-private visibility enforces aggregate boundary
     */
    static Section createNew(String title, String description, String sectionSlug, int orderIndex, Course course) {
        guardAgainstNullOrEmpty(title, "Section title");
        guardAgainstNull(course, "Course");
        
        Section section = new Section();
        section.sectionId = UUID.randomUUID();
        section.sectionSlug = sectionSlug;
        section.title = title;
        section.description = description;
        section.orderIndex = OrderIndex.of(orderIndex);
        section.course = course;
        section.lessons = new ArrayList<>();
        section.createdAt = LocalDateTime.now();
        section.updatedAt = LocalDateTime.now();
        
        return section;
    }
    
    // ========== DOMAIN METHODS ==========
    
    public Lesson findLessonById(UUID lessonId) {
        return lessons.stream()
            .filter(l -> l.getLessonId().equals(lessonId))
            .findFirst()
            .orElseThrow(() -> new LessonNotFoundException(lessonId.toString()));
    }

    public Lesson findLessonByLessonSlug(String lessonSlug) {
        return lessons.stream()
            .filter(l -> l.getLessonSlug().equals(lessonSlug))
            .findFirst()
            .orElseThrow(() -> new LessonNotFoundException(lessonSlug));
    }

    /**
     * Update section details
     */
    public void updateDetails(String title, String description, int orderIndex) {
        guardAgainstNullOrEmpty(title, "Title");
        
        this.title = title;
        this.description = description;
        this.orderIndex = OrderIndex.of(orderIndex);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Add lesson to section
     */
    public Lesson addLesson(String title, String lessonSlug, int orderIndex) {
        guardAgainstNullOrEmpty(title, "Lesson title");
        guardAgainstDuplicateLessonTitle(title);
        
        Lesson lesson = Lesson.createNew(title, lessonSlug, orderIndex, this);
        this.lessons.add(lesson);
        this.updatedAt = LocalDateTime.now();
        
        return lesson;
    }
    
    /**
     * Remove lesson from section
     */
    public void removeLesson(UUID lessonId) {
        Lesson lesson = lessons.stream()
            .filter(l -> l.getLessonId().equals(lessonId))
            .findFirst()
            .orElseThrow(() -> new LessonNotFoundException(lessonId.toString()));
        
        this.lessons.remove(lesson);
        this.updatedAt = LocalDateTime.now();
    }

    public Lesson updateLessonInSection(UUID lessonId, String title, int orderIndex) {
        Lesson lesson = lessons.stream()
            .filter(l -> l.getLessonId().equals(lessonId))
            .findFirst()
            .orElseThrow(() -> new LessonNotFoundException(lessonId.toString()));
        lesson.updateDetails(title, orderIndex);
        return lesson;
    }

    public Lesson updateLessonInSectionSlug(String lessonSlug, String title, int orderIndex) {
        Lesson lesson = lessons.stream()
            .filter(l -> l.getLessonSlug().equals(lessonSlug))
            .findFirst()
            .orElseThrow(() -> new LessonNotFoundException(lessonSlug));
        lesson.updateDetails(title, orderIndex);
        return lesson;
    }

    /**
     * Get read-only view of lessons
     */
    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }
    
    /**
     * Check if section belongs to specific course
     */
    public boolean belongsToCourse(UUID courseId) {
        return this.course.getCourseId().equals(courseId);
    }
    
    // ========== GUARDS ==========
    
    private static void guardAgainstNull(Object value, String fieldName) {
        if (value == null) {
            throw new SectionInvariantViolationException(fieldName + " cannot be null");
        }
    }
    
    private static void guardAgainstNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new SectionInvariantViolationException(fieldName + " cannot be null or empty");
        }
    }
    
    private void guardAgainstDuplicateLessonTitle(String title) {
        boolean exists = this.lessons.stream()
            .anyMatch(l -> l.getTitle().equalsIgnoreCase(title));
        
        if (exists) {
            throw new SectionInvariantViolationException(
                "Lesson with title '" + title + "' already exists in this section"
            );
        }
    }
    
    // ========== JPA CALLBACKS ==========
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}