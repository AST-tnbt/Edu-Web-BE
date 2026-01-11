package com.se347.userservice.dtos;

import java.io.Serializable;
import java.util.UUID;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEventDto implements Serializable {
    private UUID userId;
    private String email;
    private LocalDateTime createdAt;
}