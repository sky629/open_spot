package com.kangpark.openspot.auth.controller.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component

/**
 * OAuth2 로그인 실패 핸들러
 * Google OAuth2 로그인 실패 시 오류 응답 처리
 */
@Component
class OAuth2LoginFailureHandler(
    private val objectMapper: ObjectMapper
) : SimpleUrlAuthenticationFailureHandler() {
    
    private val logger = LoggerFactory.getLogger(OAuth2LoginFailureHandler::class.java)
    
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        logger.error("OAuth2 login failed: ${exception.message}", exception)
        
        try {
            val errorCode = when (exception::class.java.simpleName) {
                "OAuth2AuthenticationException" -> "OAUTH2_AUTHENTICATION_FAILED"
                "OAuth2AuthorizationException" -> "OAUTH2_AUTHORIZATION_FAILED"
                "InsufficientAuthenticationException" -> "INSUFFICIENT_AUTHENTICATION"
                else -> "OAUTH2_LOGIN_FAILED"
            }
            
            val errorResponse = ApiResponse.error<Any>(
                ErrorResponse(
                    code = errorCode,
                    message = getErrorMessage(exception),
                    details = mapOf(
                        "error" to (exception.message ?: "Unknown error"),
                        "type" to exception::class.java.simpleName
                    )
                )
            )
            
            response.contentType = "application/json;charset=UTF-8"
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
            
        } catch (e: Exception) {
            logger.error("Error handling OAuth2 login failure", e)
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        }
    }
    
    private fun getErrorMessage(exception: AuthenticationException): String {
        return when (exception::class.java.simpleName) {
            "OAuth2AuthenticationException" -> "Google 인증에 실패했습니다. 다시 시도해 주세요."
            "OAuth2AuthorizationException" -> "Google 인증 권한이 거부되었습니다."
            "InsufficientAuthenticationException" -> "인증 정보가 충분하지 않습니다."
            else -> "로그인에 실패했습니다. 다시 시도해 주세요."
        }
    }
}