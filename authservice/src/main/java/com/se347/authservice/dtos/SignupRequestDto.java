package com.se347.authservice.dtos;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {
    String email;
    String password;
    String passwordConfirm;
}
