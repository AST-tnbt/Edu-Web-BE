package com.se347.analysticservice.dtos.responses.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGrowthAnalyticsResponseDto {
    private UUID userGrowthAnalyticsId;
    private LocalDate date;
    private Long newUsersCount;
    private Long activeUsersCount;
    private Long totalUsers;
    private Double retentionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
