package com.se347.userservice.dtos;

import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequestDto {
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private String phoneNumber;
    private String address;
}