package com.kangpark.openspot.gateway.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT에서 사용자 정보를 추출하여 X-User-Id 헤더로 추가하는 필터
 * Gateway가 JWT 인증을 완료한 후, 내부 서비스로 요청을 전달할 때 사용자 식별을 위한 헤더를 추가합니다.
 */
@Component
class JwtToHeaderFilter : OncePerRequestFilter() {

    companion object {
        const val USER_ID_HEADER = "X-User-Id"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication

        // JWT 인증이 완료된 경우에만 헤더 추가
        if (authentication is JwtAuthenticationToken) {
            val jwt = authentication.token as Jwt
            val userId = jwt.subject  // JWT의 sub claim (사용자 ID)

            if (userId != null) {
                // X-User-Id 헤더를 추가한 래퍼 요청 생성
                val wrappedRequest = HeaderAddingRequestWrapper(request, USER_ID_HEADER, userId)
                filterChain.doFilter(wrappedRequest, response)
                return
            }
        }

        // JWT 인증이 없거나 sub claim이 없는 경우 원본 요청 전달
        filterChain.doFilter(request, response)
    }

    /**
     * 헤더를 추가할 수 있는 HttpServletRequestWrapper
     */
    private class HeaderAddingRequestWrapper(
        request: HttpServletRequest,
        private val headerName: String,
        private val headerValue: String
    ) : jakarta.servlet.http.HttpServletRequestWrapper(request) {

        override fun getHeader(name: String): String? {
            return if (name.equals(headerName, ignoreCase = true)) {
                headerValue
            } else {
                super.getHeader(name)
            }
        }

        override fun getHeaders(name: String): java.util.Enumeration<String> {
            return if (name.equals(headerName, ignoreCase = true)) {
                java.util.Collections.enumeration(listOf(headerValue))
            } else {
                super.getHeaders(name)
            }
        }

        override fun getHeaderNames(): java.util.Enumeration<String> {
            val names = mutableSetOf<String>()
            super.getHeaderNames()?.let { enumeration ->
                while (enumeration.hasMoreElements()) {
                    names.add(enumeration.nextElement())
                }
            }
            names.add(headerName)
            return java.util.Collections.enumeration(names)
        }
    }
}
