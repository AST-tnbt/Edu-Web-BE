package com.se347.authservice.services.impl;

import com.se347.authservice.configs.JwtConfig;
import com.se347.authservice.dtos.*;
import com.se347.authservice.entities.User;
import com.se347.authservice.enums.Role;
import com.se347.authservice.publishers.AuthenticationEventPublisher;
import com.se347.authservice.repositories.UserRepository;
import com.se347.authservice.securities.CustomUserDetailsService;
import com.se347.authservice.securities.JwtTokenProvider;
import com.se347.authservice.securities.UserPrincipal;
import com.se347.authservice.services.AuthenticationService;
import com.se347.authservice.services.RedisTokenService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private final AuthenticationEventPublisher userCreatedPublisher;
    
    public AuthenticationServiceImpl(UserRepository userRepository, 
                                    PasswordEncoder passwordEncoder, 
                                    AuthenticationManager authenticationManager, 
                                    JwtTokenProvider tokenProvider, 
                                    CustomUserDetailsService customUserDetailsService, 
                                    RedisTokenService redisTokenService, 
                                    JwtConfig jwtConfig, 
                                    AuthenticationEventPublisher userCreatedPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.redisTokenService = redisTokenService;
        this.jwtConfig = jwtConfig;
        this.userCreatedPublisher = userCreatedPublisher;
    }

    @Transactional
    @Override
    public User signup(SignupRequestDto signupRequest) {
        // Kiểm tra email tồn tại
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Kiểm tra password và passwordConfirm có trùng hay không
        if (!signupRequest.getPassword().equals(signupRequest.getPasswordConfirm())) {
            throw new RuntimeException("Password and PasswordConfirm not exist");
        }

        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role(Role.STUDENT) // mặc định role USER
                .firstLogin(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
        user.onCreate();
    
        User createdUser = userRepository.save(user);
        
        // Gửi event đến RabbitMQ
        userCreatedPublisher.publishUserCreatedEvent(createdUser);
        return createdUser;
    }

    @Override
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
                userPrincipal.getId(),
                accessToken,
                refreshToken,
                "Bearer",
                userPrincipal.getUsername(),
                userPrincipal.getAuthorities().toString(),
                userPrincipal.isFirstLogin()
        );
    }

    @Override
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        if (redisTokenService.isBlacklisted(refreshToken)) {
            throw new RuntimeException("Refresh token is blacklisted or expired");
        }

        String email = tokenProvider.getUsernameFromToken(refreshToken);
        UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(email);

        String newAccessToken = tokenProvider.generateAccessToken(userPrincipal);

        return new RefreshTokenResponseDto(newAccessToken);
    }

    @Override
    public String logout(HttpServletRequest request, LogoutRequestDto logoutRequest) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        
        if (redisTokenService.isBlacklisted(header.substring(7))) {
            throw new RuntimeException("Access token is blacklisted or expired");
        }

        String accessToken = header.substring(7); // Bỏ "Bearer "
        String refreshToken = logoutRequest.getRefreshToken();

        // Kiểm tra access token hợp lệ
        if (!tokenProvider.validateToken(accessToken)) {
            throw new SecurityException("Invalid or expired access token");
        }

        // Tính thời gian còn lại của access token
        long accessTokenRemainingMillis = tokenProvider.getRemainingTime(accessToken);

        // Đưa access token vào blacklist trong Redis
        redisTokenService.blacklistToken(accessToken, accessTokenRemainingMillis);

        // Xử lý refresh token nếu có
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            // Kiểm tra refresh token hợp lệ
            if (tokenProvider.validateToken(refreshToken)) {
                // Tính thời gian còn lại của refresh token
                long refreshTokenRemainingMillis = tokenProvider.getRemainingTime(refreshToken);
                
                // Đưa refresh token vào blacklist trong Redis
                redisTokenService.blacklistToken(refreshToken, refreshTokenRemainingMillis);
            }
        }

        // Xóa cache tokens trong Redis (nếu có)
        String username = tokenProvider.getUsernameFromToken(accessToken);
        redisTokenService.deleteToken("access:" + username);
        redisTokenService.deleteToken("refresh:" + username);

        return "Logged out successfully";
    }

    @Override
    public void setUserFirstLoginFalse(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setFirstLogin(false);
        userRepository.save(user);
    }
}
