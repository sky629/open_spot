package com.kangpark.openspot.auth.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

/**
 * JWT를 Spring Security Authentication으로 변환
 * OAuth2 Resource Server에서 사용
 */
@Component
class JwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        // JWT에서 사용자 ID 추출 (subject claim)
        val userId = jwt.subject

        // 권한 설정 (기본: ROLE_USER)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

        // JwtAuthenticationToken 생성
        return JwtAuthenticationToken(jwt, authorities, userId)
    }
}
