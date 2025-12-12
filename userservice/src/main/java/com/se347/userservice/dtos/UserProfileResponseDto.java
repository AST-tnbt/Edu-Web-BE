package com.se347.userservice.dtos;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponseDto {
    private UUID userId;
    private String userSlug;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String bio;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
