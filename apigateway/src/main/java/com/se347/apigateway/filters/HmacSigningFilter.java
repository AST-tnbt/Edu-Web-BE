package com.se347.apigateway.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
public class HmacSigningFilter extends AbstractGatewayFilterFactory<Object> {

    @Value("${gateway.hmac.secret}")
    private String secret;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            var method = request.getMethod();
            var url = request.getURI().getPath();

            return exchange.getRequest().getBody()
                    .collectList()
                    .flatMap(dataBuffers -> {
                        StringBuilder bodyBuilder = new StringBuilder();
                        dataBuffers.forEach(buffer -> {
                            byte[] bytes = new byte[buffer.readableByteCount()];
                            buffer.read(bytes);
                            bodyBuilder.append(new String(bytes, StandardCharsets.UTF_8));
                        });
                        String body = bodyBuilder.toString();

                        // Prepare components
                        String nonce = UUID.randomUUID().toString();
                        String timestamp = String.valueOf(Instant.now().getEpochSecond());
                        String payload = method + "\n" + url + "\n" + body + "\n" + nonce + "\n" + timestamp;

                        // Compute HMAC-SHA256
                        String hmacSignature = computeHmacSHA256(secret, payload);

                        // Add headers
                        var mutatedRequest = request.mutate()
                                .header("HMAC-AUTH", hmacSignature)
                                .header("HMAC-NONCE", nonce)
                                .header("HMAC-TIMESTAMP", timestamp)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });
        };
    }

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
}
