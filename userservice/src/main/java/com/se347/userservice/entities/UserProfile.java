package com.se347.userservice.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 255)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    private LocalDateTime createdAt;
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
