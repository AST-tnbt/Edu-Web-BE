package com.se347.authservice.services;

import com.se347.authservice.dtos.*;
import com.se347.authservice.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public interface AuthenticationService {

    User signup(SignupRequestDto signupRequest);
    LoginResponseDto authenticate(LoginRequestDto loginRequest);
    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequest);
    String logout(HttpServletRequest request, LogoutRequestDto logoutRequest);
    void setUserFirstLoginFalse(UUID userId);
}
