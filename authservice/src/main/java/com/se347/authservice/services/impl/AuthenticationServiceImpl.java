package com.se347.authservice.services.impl;

import com.se347.authservice.configs.JwtConfig;
import com.se347.authservice.dtos.LoginRequestDto;
import com.se347.authservice.dtos.LoginResponseDto;
import com.se347.authservice.dtos.RefreshTokenRequestDto;
import com.se347.authservice.dtos.SignupRequestDto;
import com.se347.authservice.entities.User;
import com.se347.authservice.enums.Role;
import com.se347.authservice.repositories.UserRepository;
import com.se347.authservice.securities.CustomUserDetailsService;
import com.se347.authservice.securities.JwtTokenProvider;
import com.se347.authservice.securities.UserPrincipal;
import com.se347.authservice.services.AuthenticationService;
import com.se347.authservice.services.RedisTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JwtTokenProvider tokenProvider;

    @Autowired
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    private final RedisTokenService redisTokenService;

    @Autowired
    private final JwtConfig jwtConfig;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService, RedisTokenService redisTokenService, JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.redisTokenService = redisTokenService;
        this.jwtConfig = jwtConfig;
    }

    public User signup(SignupRequestDto signupRequest) {
        // Kiểm tra email tồn tại
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role(Role.STUDENT) // mặc định role USER
                .build();

        return userRepository.save(user);
    }

    public LoginResponseDto authenticate(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getUsername());

        redisTokenService.saveToken("access:" + userPrincipal.getUsername(), accessToken, jwtConfig.getAccessTokenExpiration());
        redisTokenService.saveToken("refresh:" + userPrincipal.getUsername(), refreshToken, jwtConfig.getRefreshTokenExpiration());

        return new LoginResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                userPrincipal.getUsername(),
                userPrincipal.getAuthorities().toString()
        );
    }

    public LoginResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String email = tokenProvider.getUsernameFromToken(refreshToken);
        UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(email);

        String newAccessToken = tokenProvider.generateAccessToken(userPrincipal);

        return new LoginResponseDto(
                newAccessToken,
                refreshToken,
                "Bearer",
                userPrincipal.getUsername(),
                userPrincipal.getAuthorities().toString()
        );
    }

    public String logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String accessToken = header.substring(7); // Bỏ "Bearer "

        // Kiểm tra token hợp lệ
        if (!tokenProvider.validateToken(accessToken)) {
            throw new SecurityException("Invalid or expired access token");
        }

        // Tính thời gian còn lại của token
        long remainingMillis = tokenProvider.getRemainingTime(accessToken);

        // Đưa vào blacklist trong Redis
        redisTokenService.blacklistToken(accessToken, remainingMillis);

        // Xóa cache access token trong Redis (nếu có)
        String username = tokenProvider.getUsernameFromToken(accessToken);
        redisTokenService.deleteToken("access:" + username);

        return "Logged out successfully";
    }
}
