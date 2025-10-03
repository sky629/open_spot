package com.kangpark.openspot.location.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
open class LocationSecurityConfig {

    @Bean
    @Primary
    open fun locationSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests
                    // Public endpoints - no authentication required
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/api/v1/locations/health",
                        "/api/v1/categories"
                    ).permitAll()
                    // Protected endpoints - JWT authentication required
                    .requestMatchers(
                        "/api/v1/locations/**",
                        "/api/v1/reviews/**"
                    ).authenticated()
                    // All other requests require authentication
                    .anyRequest().authenticated()
            }
            .build()
    }
}