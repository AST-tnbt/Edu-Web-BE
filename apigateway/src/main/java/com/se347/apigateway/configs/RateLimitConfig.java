package com.se347.apigateway.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest()
                .getRemoteAddress())
            .map(addr -> addr.getAddress().getHostAddress())
            .switchIfEmpty(Mono.just("unknown"));
    }
}


