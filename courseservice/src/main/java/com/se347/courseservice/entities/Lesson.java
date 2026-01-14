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
@Table(name = "lessons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lesson {
    
    @Id
    private UUID lessonId;

    @Column(name = "lesson_slug", unique = true, nullable = false)
    private String lessonSlug;

    @Column(nullable = false)
    private String title;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "order_index", nullable = false))
    })
    private OrderIndex orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Content> contents = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ========== FACTORY METHOD (PACKAGE-PRIVATE) ==========
    
    /**
     * Create new lesson (can only be called from Section entity)
     * Package-private visibility enforces aggregate boundary
     */
    static Lesson createNew(String title, String lessonSlug, int orderIndex, Section section) {
        guardAgainstNullOrEmpty(title, "Lesson title");
        guardAgainstNull(section, "Section");
        
        Lesson lesson = new Lesson();
        lesson.lessonId = UUID.randomUUID();
        lesson.lessonSlug = lessonSlug;
        lesson.title = title;
        lesson.orderIndex = OrderIndex.of(orderIndex);
        lesson.section = section;
        lesson.contents = new ArrayList<>();
        lesson.createdAt = LocalDateTime.now();
        lesson.updatedAt = LocalDateTime.now();
        
        return lesson;
    }
    
    // ========== DOMAIN METHODS ==========
    
    /**
     * Update lesson details
     */
    public void updateDetails(String title, int orderIndex) {
        guardAgainstNullOrEmpty(title, "Title");
        
        this.title = title;
        this.orderIndex = OrderIndex.of(orderIndex);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Add content to lesson
     */
    public Content addContent(
        String contentUrl,
        int orderIndex
    ) {
        Content content = Content.createNew(
            contentUrl,
            orderIndex,
            this
        );
        
        this.contents.add(content);
        this.updatedAt = LocalDateTime.now();
        
        return content;
    }
    
    /**
     * Remove content from lesson
     */
    public void removeContent(UUID contentId) {
        Content content = contents.stream()
            .filter(c -> c.getContentId().equals(contentId))
            .findFirst()
            .orElseThrow(() -> new ContentNotFoundException(contentId.toString()));
        
        this.contents.remove(content);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update content details
     */
    public Content updateContent(UUID contentId, String contentUrl, int orderIndex) {
        Content content = contents.stream()
            .filter(c -> c.getContentId().equals(contentId))
            .findFirst()
            .orElseThrow(() -> new ContentNotFoundException(contentId.toString()));
        content.updateDetails(contentUrl, orderIndex);
        return content;
    }



    /**
     * Get read-only view of contents
     */
    public List<Content> getContents() {
        return Collections.unmodifiableList(contents);
    }
    
    /**
     * Check if lesson belongs to specific section
     */
    public boolean belongsToSection(UUID sectionId) {
        return this.section.getSectionId().equals(sectionId);
    }
    
    /**
     * Check if lesson has any content
     */
    public boolean hasContent() {
        return !this.contents.isEmpty();
    }
    
    /**
     * Get total content count
     */
    public int getContentCount() {
        return this.contents.size();
    }
    
    public Content findContentById(UUID contentId) {
        return contents.stream()
            .filter(c -> c.getContentId().equals(contentId))
            .findFirst()
            .orElseThrow(() -> new ContentNotFoundException(contentId.toString()));
    }


    /**
     * Authorization: Check if this lesson is owned by the given user
     * 
     * Strategy: Delegate to Course aggregate root
     * - Lesson → Section → Course
     * - Course enforces ownership
     */
    public void ensureOwnedBy(UUID userId) {
        if (this.section == null) {
            throw new LessonInvariantViolationException(
                "Lesson is not properly linked to a Section"
            );
        }
        
        // Delegate authorization to Course aggregate root
        this.section.getCourse().ensureOwnedBy(userId);
    }
    
    // ========== GUARDS ==========
    
    private static void guardAgainstNull(Object value, String fieldName) {
        if (value == null) {
            throw new LessonInvariantViolationException(fieldName + " cannot be null");
        }
    }
    
    private static void guardAgainstNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new LessonInvariantViolationException(fieldName + " cannot be null or empty");
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
