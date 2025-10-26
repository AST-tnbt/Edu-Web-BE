package com.se347.authservice.dtos;

import java.util.UUID;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponseDto {
    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String email;
    private String role;
    private boolean firstLogin;
}
