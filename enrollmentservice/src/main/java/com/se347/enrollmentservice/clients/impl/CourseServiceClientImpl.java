package com.se347.enrollmentservice.clients.impl;

import com.se347.enrollmentservice.clients.CourseServiceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CourseServiceClientImpl implements CourseServiceClient{
    private final WebClient courseServiceClient;
    private static final Logger logger = LoggerFactory.getLogger(CourseServiceClientImpl.class);

    @Value("${gateway.hmac.secret}")
    private String hmacSecret;

    @Value("${gateway.hmac.enabled:true}")
    private boolean hmacEnabled;

    public Integer getTotalLessonsByCourseId(UUID courseId) {
        try {
            String path = "/api/courses/" + courseId + "/total-lessons";
            return courseServiceClient.get()
                .uri(path)
                .headers(headers -> applyHmacHeaders(headers, HttpMethod.GET, path, new byte[0]))
                .retrieve()
                .bodyToMono(Integer.class)
                .timeout(Duration.ofSeconds(10))
                .blockOptional()
                .orElse(0);
        } catch (Exception e) {
            logger.warn("Failed to get totalLessons from CourseService, using default 0", e);
            throw new RuntimeException("Failed to get totalLessons from CourseService", e);
        }
    }

    private void applyHmacHeaders(HttpHeaders headers, HttpMethod method, String path, byte[] bodyBytes) {
        if (!hmacEnabled) {
            return;
        }

        String bodyHash = base64Sha256(bodyBytes);
        String nonce = UUID.randomUUID().toString();
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String payload = method.name() + "\n" + path + "\n" + bodyHash + "\n" + nonce + "\n" + timestamp;
        String hmacSignature = computeHmacSHA256(hmacSecret, payload);

        headers.add("HMAC-AUTH", hmacSignature);
        headers.add("HMAC-NONCE", nonce);
        headers.add("HMAC-TIMESTAMP", timestamp);
        headers.add("X-Body-SHA256", bodyHash);
        headers.add("X-Gateway-Signed", "true");
    }

    private String base64Sha256(byte[] data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SHA-256 hash", e);
        }
    }

    private String computeHmacSHA256(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC signature", e);
        }
    }
}
