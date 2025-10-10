package com.eduweb.apigateway.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtEmailHeaderFilter extends AbstractGatewayFilterFactory<JwtEmailHeaderFilter.Config>
        implements GlobalFilter, Ordered {

    // Đọc secret key từ file application.yml
    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtEmailHeaderFilter() {
        super(Config.class);
    }

    public static class Config {
        // Không cần cấu hình thêm, nhưng để mở rộng sau này
    }

    /**
     * Dành cho cấu hình cụ thể trong route (nếu dùng @Bean routeLocator)
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> setEmailHeader(exchange)
                .flatMap(modifiedExchange -> chain.filter(modifiedExchange));
    }

    /**
     * Dành cho Global Filter (áp dụng cho mọi route)
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return setEmailHeader(exchange)
                .flatMap(modifiedExchange -> chain.filter(modifiedExchange));
    }

    /**
     * Trích xuất email từ JWT và chèn vào header "X-User-Email"
     */
    private Mono<ServerWebExchange> setEmailHeader(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = extractSubject(token);

            if (email != null && !email.isEmpty()) {
                // Phải tạo request mới (immutable)
                var mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", email)
                        .build();

                // Tạo exchange mới để giữ request đã mutate
                return Mono.just(exchange.mutate().request(mutatedRequest).build());
            }
        }
        return Mono.just(exchange);
    }

    /**
     * Giải mã JWT và lấy subject (ở đây là email)
     */
    private String extractSubject(String token) {
        try {
            // Secret key phải được Base64 encode trong cấu hình
            SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject(); // thường là email hoặc username
        } catch (Exception e) {
            // Tránh throw lỗi để request vẫn đi qua Gateway
            return null;
        }
    }

    /**
     * Đảm bảo filter này chạy sớm (trước các route khác)
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
