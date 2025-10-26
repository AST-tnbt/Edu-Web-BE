package com.se347.authservice.dtos;

import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEventDto implements Serializable {
    private UUID userId;
    private String email;
}