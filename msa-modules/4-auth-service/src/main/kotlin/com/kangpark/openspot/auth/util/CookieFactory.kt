package com.kangpark.openspot.auth.util

import com.kangpark.openspot.auth.config.CookieConfig
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

/**
 * 쿠키 생성 팩토리
 * Refresh Token용 HttpOnly 쿠키 생성 관리
 * Access Token은 Response Body로 전달 (Bearer 헤더 방식)
 */
@Component
class CookieFactory(
    private val cookieConfig: CookieConfig
) {
    companion object {
        const val REFRESH_TOKEN_NAME = "refresh_token"
    }

    /**
     * Refresh Token 쿠키를 응답에 추가 (SameSite 포함)
     * HttpOnly=true로 설정하여 JavaScript에서 접근 불가 (XSS 방어)
     * @param token JWT Refresh Token
     * @param response HttpServletResponse
     */
    fun addRefreshTokenCookie(token: String, response: HttpServletResponse) {
        val cookie = createResponseCookie(
            name = REFRESH_TOKEN_NAME,
            value = token,
            maxAge = cookieConfig.refreshTokenMaxAge.toLong()
        )
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    /**
     * Refresh Token 쿠키 삭제 (maxAge=0)
     * @param response HttpServletResponse
     */
    fun deleteRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = createResponseCookie(
            name = REFRESH_TOKEN_NAME,
            value = "",
            maxAge = 0
        )
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    /**
     * ResponseCookie 생성 (SameSite 지원)
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 쿠키 만료 시간 (초)
     * @return ResponseCookie
     */
    private fun createResponseCookie(
        name: String,
        value: String,
        maxAge: Long
    ): ResponseCookie {
        return ResponseCookie.from(name, value)
            .httpOnly(true)  // Refresh Token은 항상 HttpOnly
            .secure(cookieConfig.secure)
            .path(cookieConfig.path)
            .maxAge(maxAge)
            .sameSite(cookieConfig.sameSite)
            .apply { cookieConfig.domain?.let { domain(it) } }
            .build()
    }
}
