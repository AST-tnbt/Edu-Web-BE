package com.se347.enrollmentservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@Configuration
public class WebClientConfig {

    @LoadBalanced    
    @Bean
    public WebClient courseServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${enrollmentservice.course.base-url}") String baseUrl
    ) {
        return webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }
}

