package com.se347.authservice.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseDto {
    private String newAccessToken;
}