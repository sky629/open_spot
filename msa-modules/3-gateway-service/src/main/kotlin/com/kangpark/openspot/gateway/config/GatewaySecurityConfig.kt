package com.kangpark.openspot.gateway.config

import com.kangpark.openspot.gateway.filter.JwtToHeaderFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
open class GatewaySecurityConfig(
    private val jwtToHeaderFilter: JwtToHeaderFilter
) {

    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

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
                        "/oauth2/**",
                        "/login/oauth2/**"
                    ).permitAll()
                    // Location service public endpoints
                    .requestMatchers(
                        "/api/v1/locations/health",
                        "/api/v1/categories"
                    ).permitAll()
                    // All other requests require JWT authentication
                    .anyRequest().authenticated()
            }
            // OAuth2 Resource Server - JWT 검증
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            // JWT에서 사용자 ID 추출하여 X-User-Id 헤더 추가
            .addFilterAfter(jwtToHeaderFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    open fun jwtDecoder(): JwtDecoder {
        // JWT secret을 직접 사용하여 HMAC SHA256 키 생성
        val secretKey: SecretKey = SecretKeySpec(jwtSecret.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(secretKey).build()
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