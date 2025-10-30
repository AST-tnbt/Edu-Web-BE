package com.se347.courseservice.entities;

import com.se347.courseservice.enums.ContentType;
import com.se347.courseservice.enums.ContentStatus;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;


@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID contentId;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false)
    private ContentType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contentUrl;

    @Column(nullable = false)
    private String textContent;

    @Column(nullable = false)
    private int orderIndex;

    @Column(nullable = false, updatable = false)
    private ContentStatus status;

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
