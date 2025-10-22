package com.se347.apigateway.filters;

import com.se347.apigateway.configs.JwtConfig;
import com.se347.apigateway.exceptions.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Filter cho API Gateway
 *
 * Filter này sẽ:
 * 1. Kiểm tra JWT token trong Authorization header
 * 2. Validate token
 * 3. Thêm thông tin user vào request headers
 * 4. Cho phép hoặc từ chối request
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Lấy path của request
            String path = request.getURI().getPath();

            // Bỏ qua các path không cần xác thực
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // Lấy token từ Authorization header
            String token = getTokenFromRequest(request);

            if (!StringUtils.hasText(token)) {
                throw new JwtException.JwtTokenMissingException("Authorization token is required");
            }
            
            // Kiểm tra token có trong blacklist
            if (redisTemplate.hasKey("blacklist:" + token)) {
                throw new JwtException.JwtTokenInvalidException("Token is blacklisted or expired");
            }

            try {
                // Validate token chữ ký/hạn
                if (!jwtConfig.isTokenValid(token)) {
                    return Mono.error(new JwtException.JwtTokenInvalidException("Token is invalid or expired"));
                }

                // Lấy thông tin user từ token
                String username = jwtConfig.getUsernameFromToken(token);
                String userId = jwtConfig.getUserIdFromToken(token);
                logger.debug("UserId: {}", userId);
                java.util.List<String> roles = jwtConfig.getRolesFromToken(token);

                // Sanitize: remove any spoofed identity headers from client
                ServerHttpRequest sanitized = request.mutate()
                    .headers(h -> {
                        h.remove("X-User-Id");
                        h.remove("X-Username");
                        h.remove("X-User-Roles");
                        h.remove("X-Authenticated");
                    })
                    .build();

                // Thêm thông tin user vào request headers (chỉ do gateway đặt)
                ServerHttpRequest modifiedRequest = sanitized.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .header("X-User-Roles", String.join(",", roles))
                    .header("X-Authenticated", "true")
                    .build();

                // Tiếp tục với request đã được modify
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (JwtException e) {
                return Mono.error(e);
            } catch (Exception e) {
                logger.error("JWT validation failed", e);
                return Mono.error(new JwtException.JwtTokenInvalidException("Token validation failed: " + e.getMessage(), e));
            }
        };
    }

    /**
     * Lấy token từ Authorization header
     */
    private String getTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Kiểm tra path có phải là public path không
     */
    private boolean isPublicPath(String path) {
        List<String> publicPaths = List.of(
                "/api/auth/login",
                "/api/auth/signup",
                "/api/auth/refresh"
        );

        return publicPaths.stream()
                .anyMatch(path::startsWith);
    }


    /**
     * Configuration class cho filter
     */
    public static class Config {
        // Có thể thêm các config properties ở đây nếu cần
    }
}
