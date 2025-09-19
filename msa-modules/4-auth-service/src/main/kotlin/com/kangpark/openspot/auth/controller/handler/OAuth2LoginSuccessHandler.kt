package com.kangpark.openspot.auth.controller.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangpark.openspot.auth.service.AuthApplicationService
import com.kangpark.openspot.auth.service.usecase.LoginUseCase
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
 * Google OAuth2 로그인 성공 시 JWT 토큰 발급 및 응답 처리
 */
@Component
class OAuth2LoginSuccessHandler(
    private val authApplicationService: AuthApplicationService,
    private val objectMapper: ObjectMapper,
    @param:Value("\${app.frontend.base-url}")
    private val frontendBaseUrl: String,
    @param:Value("\${app.frontend.auth-success-path}")
    private val authSuccessPath: String
) : SimpleUrlAuthenticationSuccessHandler() {
    private val logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler::class.java)


    fun createSuccessRedirectUrl(loginResult: LoginUseCase.LoginResult): String {
        return UriComponentsBuilder.fromUriString(frontendBaseUrl+authSuccessPath)
            .queryParam("token", loginResult.accessToken)
            .queryParam("refresh_token", loginResult.refreshToken)
            .queryParam("user", loginResult.isNewUser)
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

            response.sendRedirect(createSuccessRedirectUrl(loginResult))

            logger.info(
                "Login successful for user: ${loginResult.user.email} (ID: ${loginResult.user.id}, New User: ${loginResult.isNewUser})"
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