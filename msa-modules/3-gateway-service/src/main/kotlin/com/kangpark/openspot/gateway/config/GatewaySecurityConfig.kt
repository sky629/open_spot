package com.kangpark.openspot.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
open class GatewaySecurityConfig {

    @Bean
    open fun gatewaySecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { handling ->
                handling.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .authorizeHttpRequests { requests ->
                requests
                    // Actuator endpoints - no authentication required
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/actuator/gateway/**"
                    ).permitAll()
                    // Auth service public endpoints
                    .requestMatchers(
                        "/api/v1/auth/health",
                        "/api/v1/auth/google/url",
                        "/oauth2/**",
                        "/login/oauth2/**"
                    ).permitAll()
                    // Location service health endpoint
                    .requestMatchers(
                        "/api/v1/locations/health"
                    ).permitAll()
                    // All other requests - Gateway는 라우팅만 담당, 인증은 각 서비스에서 처리
                    .anyRequest().permitAll()
            }
            .build()
    }

    @Bean
    open fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "http://localhost:3000", // 로컬 프론트엔드 개발 서버
            "https://open-spot.com" // 실제 프로덕션 프론트엔드 도메인
        )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}