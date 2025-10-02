package com.kangpark.openspot.auth.controller.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangpark.openspot.auth.service.AuthApplicationService
import com.kangpark.openspot.auth.util.CookieFactory
import com.kangpark.openspot.common.web.dto.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

/**
 * OAuth2 로그인 성공 핸들러
 * Google OAuth2 로그인 성공 시 JWT 토큰 발급 및 HttpOnly 쿠키로 전달
 */
@Component
class OAuth2LoginSuccessHandler(
    private val authApplicationService: AuthApplicationService,
    private val cookieFactory: CookieFactory,
    private val objectMapper: ObjectMapper,
    @param:Value("\${app.frontend.base-url}")
    private val frontendBaseUrl: String,
    @param:Value("\${app.frontend.auth-success-path}")
    private val authSuccessPath: String,
    @param:Value("\${app.frontend.allowed-redirect-uris:}")
    private val allowedRedirectUris: List<String>
) : SimpleUrlAuthenticationSuccessHandler() {
    private val logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler::class.java)

    companion object {
        const val REDIRECT_URI_SESSION_KEY = "OAUTH2_REDIRECT_URI"
    }

    /**
     * redirect_uri 화이트리스트 검증
     * Open Redirect 공격 방지
     */
    private fun isAllowedRedirectUri(uri: String): Boolean {
        // 기본 프론트엔드 URL은 항상 허용
        if (uri.startsWith(frontendBaseUrl)) {
            return true
        }

        // 화이트리스트에 등록된 URL만 허용
        return allowedRedirectUris.any { allowed ->
            uri.startsWith(allowed)
        }
    }

    /**
     * Session에서 redirect_uri 추출 및 검증
     */
    private fun getValidatedRedirectUri(request: HttpServletRequest): String {
        val sessionRedirectUri = request.session.getAttribute(REDIRECT_URI_SESSION_KEY) as? String

        return if (sessionRedirectUri != null && isAllowedRedirectUri(sessionRedirectUri)) {
            logger.debug("Using validated redirect_uri from session: $sessionRedirectUri")
            // Session에서 제거 (일회용)
            request.session.removeAttribute(REDIRECT_URI_SESSION_KEY)
            sessionRedirectUri
        } else {
            if (sessionRedirectUri != null) {
                logger.warn("Rejected invalid redirect_uri: $sessionRedirectUri")
            }
            // 기본값 사용
            frontendBaseUrl + authSuccessPath
        }
    }

    /**
     * 성공 리다이렉트 URL 생성
     * Access Token을 쿼리 파라미터로 전달 (프론트엔드에서 메모리/localStorage에 저장)
     */
    private fun createSuccessRedirectUrl(baseUri: String, accessToken: String, isNewUser: Boolean): String {
        return UriComponentsBuilder.fromUriString(baseUri)
            .queryParam("token", accessToken)
            .queryParam("new_user", isNewUser)
            .build()
            .toUriString()
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val oauth2User = authentication.principal as OAuth2User

            logger.info("OAuth2 login success for user: ${oauth2User.getAttribute<String>("email")}")

            val loginResult = authApplicationService.login(oauth2User)

            // Refresh Token만 HttpOnly 쿠키로 설정 (XSS 방어)
            cookieFactory.addRefreshTokenCookie(loginResult.refreshToken, response)

            // Session에서 검증된 redirect_uri 가져오기 (없으면 기본값)
            val redirectUri = getValidatedRedirectUri(request)

            // Access Token은 URL 쿼리 파라미터로 전달 (프론트엔드에서 메모리 저장)
            val redirectUrl = createSuccessRedirectUrl(redirectUri, loginResult.accessToken, loginResult.isNewUser)
            response.sendRedirect(redirectUrl)

            logger.info(
                "Login successful for user: ${loginResult.user.email} (ID: ${loginResult.user.id}, New User: ${loginResult.isNewUser}), Redirect to: $redirectUrl"
            )

        } catch (e: Exception) {
            logger.error("OAuth2 login success handling failed", e)
            handleException(response, e)
        }
    }

    private fun handleException(response: HttpServletResponse, e: Exception) {
        try {
            val errorResponse = ApiResponse.error<Any>(
                com.kangpark.openspot.common.web.dto.ErrorResponse(
                    code = "OAUTH2_LOGIN_ERROR",
                    message = "로그인 처리 중 오류가 발생했습니다: ${e.message}"
                )
            )

            response.contentType = "application/json;charset=UTF-8"
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        } catch (ex: Exception) {
            logger.error("Error handling exception in OAuth2LoginSuccessHandler", ex)
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        }
    }
}