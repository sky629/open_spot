package com.kangpark.openspot.auth.config

import com.kangpark.openspot.auth.controller.handler.OAuth2LoginFailureHandler
import com.kangpark.openspot.auth.controller.handler.OAuth2LoginSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
open class AuthSecurityConfig(
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val oAuth2LoginFailureHandler: OAuth2LoginFailureHandler
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
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    // 토큰 갱신은 인증 없이 허용 (리프레시 토큰으로 검증)
                    .requestMatchers("/api/v1/auth/token/refresh").permitAll()
                    // Gateway에서 JWT 검증 완료 후 X-User-Id 헤더 추가하므로 모든 요청 허용
                    .requestMatchers("/api/v1/users/**").permitAll()
                    .requestMatchers("/api/v1/auth/logout").permitAll()
                    .anyRequest().permitAll()
            }
            // OAuth2 Client 설정 (Google 로그인)
            .oauth2Login { oauth2 ->
                oauth2
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler)
            }
            .build()
    }
}