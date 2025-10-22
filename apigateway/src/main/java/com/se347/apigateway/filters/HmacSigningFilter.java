package com.se347.apigateway.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import com.se347.apigateway.utils.HashUtil;
import com.se347.apigateway.exceptions.HmacException;

/**
 * HMAC Signing Filter cho API Gateway
 * 
 * Filter này sẽ:
 * 1. Tạo HMAC signature cho request đi từ Gateway đến các services
 * 2. Thêm HMAC headers vào request để services có thể xác thực
 * 3. Đảm bảo tính toàn vẹn dữ liệu giữa Gateway và services
 */
@Component
public class HmacSigningFilter extends AbstractGatewayFilterFactory<HmacSigningFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(HmacSigningFilter.class);

    @Value("${gateway.hmac.secret}")
    private String secret;

    public HmacSigningFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String method = request.getMethod().name();
            String url = request.getURI().getPath();

            // Chỉ ký request cho các path cần thiết
            if (!shouldSignRequest(url)) {
                return chain.filter(exchange);
            }

            logger.debug("HMAC signing for path: {}", url);
            
            return exchange.getRequest().getBody()
            .reduce(exchange.getResponse().bufferFactory().wrap(new byte[0]), (prev, next) -> {
                byte[] a = new byte[prev.readableByteCount()];
                prev.read(a);
                byte[] b = new byte[next.readableByteCount()];
                next.read(b);
                byte[] merged = new byte[a.length + b.length];
                System.arraycopy(a, 0, merged, 0, a.length);
                System.arraycopy(b, 0, merged, a.length, b.length);
                return exchange.getResponse().bufferFactory().wrap(merged);
            })
            .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
            .flatMap(buffer -> {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);

                String bodyHash = HashUtil.base64Sha256(bytes);
                String nonce = UUID.randomUUID().toString();
                String timestamp = String.valueOf(Instant.now().getEpochSecond());
                String payload = method + "\n" + url + "\n" + bodyHash + "\n" + nonce + "\n" + timestamp;
                String hmacSignature = computeHmacSHA256(secret, payload);

                logger.debug("HMAC signing - Method: {}, URL: {}, BodyHash: {}, Nonce: {}, Timestamp: {}", 
                           method, url, bodyHash, nonce, timestamp);
                logger.debug("HMAC signing payload: {}", payload);
                logger.debug("HMAC signature: {}", hmacSignature);

                ServerHttpRequest decorated = new org.springframework.http.server.reactive.ServerHttpRequestDecorator(request) {
                    @Override
                    public @org.springframework.lang.NonNull reactor.core.publisher.Flux<org.springframework.core.io.buffer.DataBuffer> getBody() {
                        org.springframework.core.io.buffer.DataBufferFactory f = exchange.getResponse().bufferFactory();
                        return reactor.core.publisher.Flux.just(f.wrap(bytes));
                    }
                };

                ServerHttpRequest mutatedRequest = decorated.mutate()
                    .header("HMAC-AUTH", hmacSignature)
                    .header("HMAC-NONCE", nonce)
                    .header("HMAC-TIMESTAMP", timestamp)
                    .header("X-Gateway-Signed", "true")
                    .header("X-Body-SHA256", bodyHash)
                    .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            })
            .onErrorResume(throwable -> {
                logger.error("HMAC signing failed", throwable);
                throw new HmacException.HmacSigningException("Failed to sign request", throwable);
            });
        };
    }

    /**
     * Kiểm tra xem có nên ký request này không
     * Chỉ ký request đi đến các services (không ký public endpoints)
     */
    private boolean shouldSignRequest(String path) {
        // Ký tất cả request đi đến services trừ public endpoints
        return !path.startsWith("/actuator/") && 
               !path.startsWith("/api/auth/login") &&
               !path.startsWith("/api/auth/signup") &&
               !path.startsWith("/api/auth/refresh");
    }

    /**
     * Compute HMAC-SHA256 signature
     */
    private String computeHmacSHA256(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    /**
     * Configuration class cho filter
     */
    public static class Config {
        // Có thể thêm các config properties ở đây nếu cần
    }
}
