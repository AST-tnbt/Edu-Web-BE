package com.se347.authservice.securities;

import com.se347.authservice.configs.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtConfig.getSecret()));
    }

    // Tạo Access Token
    public String generateAccessToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());

        List<String> roles = userPrincipal.getAuthorities()
                .stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId().toString())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Tạo Refresh Token
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy username
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // Lấy roles
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // Kiểm tra token hợp lệ
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Invalid token: " + e.getMessage());
        }
        return false;
    }

    // Parse claims
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getRemainingTime(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
}
