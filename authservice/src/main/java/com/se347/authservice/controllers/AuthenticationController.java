package com.se347.authservice.controllers;

import com.se347.authservice.dtos.LoginRequestDto;
import com.se347.authservice.dtos.LogoutRequestDto;
import com.se347.authservice.dtos.RefreshTokenRequestDto;
import com.se347.authservice.dtos.SignupRequestDto;
import com.se347.authservice.entities.User;
import com.se347.authservice.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private final AuthenticationService authenticationService;

    AuthenticationController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto signupRequest) {
        User user = authenticationService.signup(signupRequest);
        return ResponseEntity.ok("Signup successful for user: " + user.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDto refreshTokenRequest) {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, @RequestBody LogoutRequestDto logoutRequest) {
            return ResponseEntity.ok(authenticationService.logout(request, logoutRequest));
    }
}
