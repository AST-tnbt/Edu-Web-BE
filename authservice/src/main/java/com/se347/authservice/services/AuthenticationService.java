package com.se347.authservice.services;

import com.se347.authservice.dtos.LoginRequestDto;
import com.se347.authservice.dtos.RefreshTokenRequestDto;
import com.se347.authservice.dtos.LogoutRequestDto;
import com.se347.authservice.dtos.SignupRequestDto;
import com.se347.authservice.dtos.LoginResponseDto;
import com.se347.authservice.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthenticationService {

    User signup(SignupRequestDto signupRequest);
    LoginResponseDto authenticate(LoginRequestDto loginRequest);
    LoginResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequest);
    String logout(HttpServletRequest request, LogoutRequestDto logoutRequest);
}
