package com.kangpark.openspot.location.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Location Service Security 설정
 * Gateway에서 JWT 인증을 완료하므로, 이 서비스는 Gateway를 신뢰합니다.
 * Gateway가 전달하는 X-User-Id 헤더를 기반으로 사용자 식별
 */
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
                    // Gateway가 JWT 검증을 완료했으므로 모든 요청 허용
                    // X-User-Id 헤더를 통해 사용자 식별
                    .anyRequest().permitAll()
            }
            .build()
    }
}