package com.kangpark.openspot.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * 쿠키 설정 관리
 * application.yml의 app.cookie 설정을 주입받아 관리
 *
 * Refresh Token만 HttpOnly Cookie로 관리
 * Access Token은 Response Body로 전달 (Bearer 헤더 방식)
 */
@Configuration
@ConfigurationProperties(prefix = "app.cookie")
class CookieConfig {
    /**
     * HTTPS 전용 여부 (true: HTTPS만 허용, false: HTTP도 허용)
     * Production: true, Development: false
     */
    var secure: Boolean = true

    /**
     * SameSite 속성 (CSRF 방어)
     * Strict: 같은 사이트에서만 전송
     * Lax: 일부 cross-site 요청 허용
     * None: 모든 요청 허용 (secure=true 필수)
     */
    var sameSite: String = "Strict"

    /**
     * 쿠키 도메인
     * null: 현재 도메인만
     * ".example.com": 서브도메인 포함
     */
    var domain: String? = null

    /**
     * 쿠키 경로
     */
    var path: String = "/"

    /**
     * Refresh Token 만료 시간 (초)
     * 기본값: 604800초 (7일)
     */
    var refreshTokenMaxAge: Int = 604800
}
