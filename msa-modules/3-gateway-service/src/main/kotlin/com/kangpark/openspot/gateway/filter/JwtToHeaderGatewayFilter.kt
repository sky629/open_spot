package com.kangpark.openspot.gateway.filter

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.servlet.function.HandlerFilterFunction
import org.springframework.web.servlet.function.HandlerFunction
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

/**
 * Spring Cloud Gateway MVC용 JWT to Header 변환 필터
 *
 * Gateway에서 JWT 인증을 완료한 후, 내부 서비스로 요청을 전달할 때
 * X-User-Id 헤더를 추가하여 서비스들이 사용자를 식별할 수 있도록 합니다.
 *
 * 표준 MSA 패턴:
 * - Gateway: JWT 검증 담당
 * - 내부 서비스: X-User-Id 헤더만 신뢰 (JWT 검증 불필요)
 */
@Component
class JwtToHeaderGatewayFilter : HandlerFilterFunction<ServerResponse, ServerResponse> {

    companion object {
        const val USER_ID_HEADER = "X-User-Id"
    }

    override fun filter(
        request: ServerRequest,
        next: HandlerFunction<ServerResponse>
    ): ServerResponse {
        val authentication = SecurityContextHolder.getContext().authentication

        // JWT 인증이 완료된 경우에만 헤더 추가
        return if (authentication is JwtAuthenticationToken) {
            val userId = authentication.token.subject  // JWT의 sub claim (사용자 ID)

            if (userId != null) {
                // X-User-Id 헤더를 추가한 새 요청 생성
                val modifiedRequest = ServerRequest.from(request)
                    .header(USER_ID_HEADER, userId)
                    .build()

                return next.handle(modifiedRequest)
            }

            next.handle(request)
        } else {
            // JWT 인증이 없는 경우 원본 요청 전달
            next.handle(request)
        }
    }
}
