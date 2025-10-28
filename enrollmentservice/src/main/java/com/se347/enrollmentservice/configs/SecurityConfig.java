package com.se347.enrollmentservice.configs;

import com.se347.enrollmentservice.securities.HmacValidationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final HmacValidationFilter hmacValidationFilter;

    SecurityConfig(HmacValidationFilter hmacValidationFilter){
        this.hmacValidationFilter = hmacValidationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(hmacValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
