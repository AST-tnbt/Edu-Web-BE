package com.se347.authservice.dtos;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    String email;
    String password;
}
