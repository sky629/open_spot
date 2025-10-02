package com.kangpark.openspot.auth.config

import com.kangpark.openspot.auth.controller.handler.OAuth2LoginFailureHandler
import com.kangpark.openspot.auth.controller.handler.OAuth2LoginSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
open class AuthSecurityConfig(
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val oAuth2LoginFailureHandler: OAuth2LoginFailureHandler,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtDecoder: JwtDecoder,
    private val jwtAuthenticationConverter: JwtAuthenticationConverter
) {
    @Bean
    @Primary
    open fun authSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { it.disable() }  // Gateway에서 CORS 처리, Auth Service는 비활성화
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests
                    // 헬스 체크 엔드포인트는 인증 없이 접근 허용
                    .requestMatchers("/api/v1/auth/health", "/actuator/health").permitAll()
                    // OAuth2 로그인 관련 엔드포인트 허용
                    .requestMatchers("/api/v1/auth/google/login", "/oauth2/**", "/login/oauth2/**").permitAll()
                    // 토큰 갱신은 인증 없이 허용 (리프레시 토큰으로 검증)
                    .requestMatchers("/api/v1/auth/token/refresh").permitAll()
                    // 사용자 관련 API는 JWT 인증 필요
                    .requestMatchers("/api/v1/users/**").authenticated()
                    // 로그아웃은 JWT 인증 필요
                    .requestMatchers("/api/v1/auth/logout").authenticated()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                // 인증 실패 시 302 리다이렉트 대신 401 JSON 응답 반환
                exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }
            // OAuth2 Resource Server 설정 (JWT Bearer 토큰 인증)
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.decoder(jwtDecoder)
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }
            // OAuth2 Client 설정 (Google 로그인)
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/api/v1/auth/google/login")
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler)
            }
            .build()
    }
}