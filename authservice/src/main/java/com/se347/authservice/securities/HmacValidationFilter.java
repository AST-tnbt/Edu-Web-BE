package com.se347.authservice.securities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * HMAC Validation Filter cho Auth Service
 * 
 * Filter này sẽ:
 * 1. Validate HMAC signature từ API Gateway
 * 2. Đảm bảo request đến từ trusted API Gateway
 * 3. Bỏ qua validation cho public endpoints
 */
@Component
@Order(1)
public class HmacValidationFilter extends OncePerRequestFilter {

    @Value("${gateway.hmac.secret}")
    private String secret;

    @Value("${gateway.hmac.enabled:true}")
    private boolean hmacEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Kiểm tra xem HMAC validation có được enable không
        if (!hmacEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Bỏ qua các path không cần HMAC validation
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy HMAC headers
        String hmacAuth = request.getHeader("HMAC-AUTH");
        String hmacNonce = request.getHeader("HMAC-NONCE");
        String hmacTimestamp = request.getHeader("HMAC-TIMESTAMP");
        String bodyHashHeader = request.getHeader("X-Body-SHA256");

        if (!StringUtils.hasText(hmacAuth) || !StringUtils.hasText(hmacNonce) || !StringUtils.hasText(hmacTimestamp)) {
            handleUnauthorized(response, "Missing HMAC headers");
            return;
        }

        try {
            // Validate timestamp (prevent replay attacks)
            long requestTime = Long.parseLong(hmacTimestamp);
            long currentTime = Instant.now().getEpochSecond();
            long timeDiff = Math.abs(currentTime - requestTime);
            
            // Allow 5 minutes tolerance
            if (timeDiff > 300) {
                handleUnauthorized(response, "Request timestamp expired");
                return;
            }

            // Đọc body để tính hash
            byte[] bodyBytes = request.getInputStream().readAllBytes();
            String computedHash = base64Sha256(bodyBytes);

            if (org.springframework.util.StringUtils.hasText(bodyHashHeader) && !bodyHashHeader.equals(computedHash)) {
                handleUnauthorized(response, "Body hash mismatch");
                return;
            }

            // Validate HMAC signature (body-hash)
            if (!validateHmacSignature(request, hmacAuth, hmacNonce, hmacTimestamp, computedHash)) {
                handleUnauthorized(response, "Invalid HMAC signature");
                return;
            }

            // Wrap lại request để downstream đọc lại body
            jakarta.servlet.http.HttpServletRequest wrapped = new jakarta.servlet.http.HttpServletRequestWrapper(request) {
                @Override
                public jakarta.servlet.ServletInputStream getInputStream() {
                    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bodyBytes);
                    return new jakarta.servlet.ServletInputStream() {
                        @Override public int read() { return bais.read(); }
                        @Override public boolean isFinished() { return bais.available() == 0; }
                        @Override public boolean isReady() { return true; }
                        @Override public void setReadListener(jakarta.servlet.ReadListener readListener) { }
                    };
                }
                @Override public int getContentLength() { return bodyBytes.length; }
                @Override public long getContentLengthLong() { return bodyBytes.length; }
            };

            // HMAC validation successful, continue with request
            filterChain.doFilter(wrapped, response);

        } catch (Exception e) {
            handleUnauthorized(response, "HMAC validation failed: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra path có phải là public path không
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login") || 
               path.startsWith("/api/auth/signup") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/actuator/");
    }

    /**
     * Validate HMAC signature
     */
    private boolean validateHmacSignature(HttpServletRequest request, String hmacAuth, 
                                        String hmacNonce, String hmacTimestamp, String bodyHash) {
        try {
            // Reconstruct the payload that was used to create the signature
            String method = request.getMethod();
            String url = request.getRequestURI();
            String payload = method + "\n" + url + "\n" + bodyHash + "\n" + hmacNonce + "\n" + hmacTimestamp;

            // Compute expected HMAC
            String expectedHmac = computeHmacSHA256(secret, payload);

            // Compare signatures
            return hmacAuth.equals(expectedHmac);

        } catch (Exception e) {
            return false;
        }
    }

    private String base64Sha256(byte[] data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            return java.util.Base64.getEncoder().encodeToString(md.digest(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
     * Xử lý khi request không được authorize
     */
    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        
        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message, new java.util.Date().toString());
        
        response.getWriter().write(body);
    }
}
