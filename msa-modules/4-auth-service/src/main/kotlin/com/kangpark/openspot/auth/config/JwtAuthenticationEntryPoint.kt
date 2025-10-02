package com.kangpark.openspot.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * JWT 인증 실패 시 JSON 응답을 반환하는 EntryPoint
 * 302 리다이렉트 대신 401 Unauthorized JSON 응답 반환
 */
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = ApiResponse.error<Any>(
            ErrorResponse(
                code = "UNAUTHORIZED",
                message = "인증이 필요합니다. Authorization 헤더에 Bearer 토큰을 포함해주세요."
            )
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
