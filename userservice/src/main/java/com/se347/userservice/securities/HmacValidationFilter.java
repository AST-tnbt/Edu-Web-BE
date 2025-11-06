package com.se347.userservice.securities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import lombok.NonNull;

/**
 * HMAC Validation Filter cho User Service
 * 
 * Filter này sẽ:
 * 1. Validate HMAC signature từ API Gateway
 * 2. Đảm bảo request đến từ trusted API Gateway
 * 3. Bỏ qua validation cho public endpoints
 */
@Component
@Order(1)
public class HmacValidationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HmacValidationFilter.class);

    @Value("${gateway.hmac.secret}")
    private String secret;

    @Value("${gateway.hmac.enabled:true}")
    private boolean hmacEnabled;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) 
                                    throws ServletException, IOException {
        // Kiểm tra xem HMAC validation có được enable không
        if (!hmacEnabled) {
            logger.debug("HMAC validation disabled, skipping");
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        logger.debug("HMAC validation for path: {}", path);

        // Bỏ qua các path không cần HMAC validation
        if (isPublicPath(path)) {
            logger.debug("Public path, skipping HMAC validation: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy HMAC headers
        String hmacAuth = request.getHeader("HMAC-AUTH");
        String hmacNonce = request.getHeader("HMAC-NONCE");
        String hmacTimestamp = request.getHeader("HMAC-TIMESTAMP");
        String bodyHashHeader = request.getHeader("X-Body-SHA256");
        String gatewaySigned = request.getHeader("X-Gateway-Signed");

        logger.debug("HMAC headers - Auth: {}, Nonce: {}, Timestamp: {}, BodyHash: {}, GatewaySigned: {}", 
                    hmacAuth != null ? "present" : "missing",
                    hmacNonce != null ? "present" : "missing", 
                    hmacTimestamp != null ? "present" : "missing",
                    bodyHashHeader != null ? "present" : "missing",
                    gatewaySigned);

        if (!StringUtils.hasText(hmacAuth) || !StringUtils.hasText(hmacNonce) || !StringUtils.hasText(hmacTimestamp)) {
            logger.warn("Missing HMAC headers for path: {}", path);
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

            // Đọc body bytes để tính hash
            byte[] bodyBytes = request.getInputStream().readAllBytes();
            String computedHash = base64Sha256(bodyBytes);

            logger.debug("Body hash - Header: {}, Computed: {}, Body length: {}", 
                        bodyHashHeader, computedHash, bodyBytes.length);

            if (org.springframework.util.StringUtils.hasText(bodyHashHeader) && !computedHash.equals(bodyHashHeader)) {
                logger.warn("Body hash mismatch for path: {} - Header: {}, Computed: {}", 
                           path, bodyHashHeader, computedHash);
                handleUnauthorized(response, "Body hash mismatch");
                return;
            }

            // Validate HMAC signature (dùng body-hash)
            if (!validateHmacSignature(request, hmacAuth, hmacNonce, hmacTimestamp, computedHash)) {
                logger.warn("Invalid HMAC signature for path: {}", path);
                handleUnauthorized(response, "Invalid HMAC signature");
                return;
            }

            logger.debug("HMAC validation successful for path: {}", path);

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
        return path.startsWith("/actuator/") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/signup") ||
               path.startsWith("/api/auth/refresh");
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

            logger.debug("HMAC validation payload: {}", payload);

            // Compute expected HMAC
            String expectedHmac = computeHmacSHA256(secret, payload);

            logger.debug("HMAC validation - Expected: {}, Received: {}", expectedHmac, hmacAuth);

            // Compare signatures
            boolean isValid = hmacAuth.equals(expectedHmac);
            logger.debug("HMAC signature valid: {}", isValid);
            
            return isValid;

        } catch (Exception e) {
            logger.error("Error validating HMAC signature", e);
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