package com.se347.courseservice.entities;

import com.se347.courseservice.entities.valueobjects.OrderIndex;
import com.se347.courseservice.exceptions.CourseException.*;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {
    
    @Id
    private UUID contentId;

    @Column(nullable = true)
    private String contentUrl;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "order_index", nullable = false))
    })
    private OrderIndex orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ========== FACTORY METHOD (PACKAGE-PRIVATE) ==========
    
    /**
     * Create new content (can only be called from Lesson entity)
     * Package-private visibility enforces aggregate boundary
     */
    static Content createNew(
        String contentUrl,
        int orderIndex,
        Lesson lesson
    ) {
        guardAgainstNull(lesson, "Lesson");
        validateContentData(contentUrl);
        
        Content content = new Content();
        content.contentId = UUID.randomUUID();
        content.contentUrl = contentUrl;
        content.orderIndex = OrderIndex.of(orderIndex);
        content.lesson = lesson;
        content.createdAt = LocalDateTime.now();
        content.updatedAt = LocalDateTime.now();
        
        return content;
    }
    
    // ========== DOMAIN METHODS ==========
    
    /**
     * Update content details
     */
    public void updateDetails(
        String contentUrl,
        int orderIndex
    ) {
        validateContentData(contentUrl);
        
        this.contentUrl = contentUrl;
        this.orderIndex = OrderIndex.of(orderIndex);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if content belongs to specific lesson
     */
    public boolean belongsToLesson(UUID lessonId) {
        return this.lesson.getLessonId().equals(lessonId);
    }
    
    // ========== GUARDS ==========
    
    private static void guardAgainstNull(Object value, String fieldName) {
        if (value == null) {
            throw new InvalidContentStateException(fieldName + " cannot be null");
        }
    }
    
    /**
     * Validate content URL
     */
    private static void validateContentData(String contentUrl) {
        if (contentUrl == null || contentUrl.trim().isEmpty()) {
            throw new InvalidContentStateException("Content URL cannot be null or empty");
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
