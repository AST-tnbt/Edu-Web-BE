package com.se347.apigateway.configs;

import com.se347.apigateway.filters.HmacSigningFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, HmacSigningFilter hmacFilter) {
        return builder.routes()
                .route("user_service", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(hmacFilter.apply(new Object())))
                        .uri("http://localhost:8006"))
                .route("auth_service", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(hmacFilter.apply(new Object())))
                        .uri("http://localhost:8005"))
                .build();
    }
}
