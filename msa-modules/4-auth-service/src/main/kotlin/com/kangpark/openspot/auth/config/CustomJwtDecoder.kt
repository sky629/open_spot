package com.kangpark.openspot.auth.config

import com.kangpark.openspot.auth.repository.external.JwtTokenProvider
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * 커스텀 JWT Decoder
 * 기존 JwtTokenProvider를 활용하여 JWT 검증 및 디코딩
 */
@Component
class CustomJwtDecoder(
    private val jwtTokenProvider: JwtTokenProvider
) : JwtDecoder {

    override fun decode(token: String): Jwt {
        try {
            // 1. JWT 유효성 검증
            if (!jwtTokenProvider.validateToken(token)) {
                throw JwtException("Invalid JWT token")
            }

            // 2. JWT Claims 추출
            val userId = jwtTokenProvider.getUserIdFromToken(token)
                ?: throw JwtException("Unable to extract user ID from token")

            val email = jwtTokenProvider.getEmailFromToken(token)
            val name = jwtTokenProvider.getNameFromToken(token)
            val expirationDate = jwtTokenProvider.getExpirationDateFromToken(token)
            val issuedDate = jwtTokenProvider.getIssuedAtFromToken(token)

            // 3. Spring Security Jwt 객체 생성
            val headers = mapOf("alg" to "HS512", "typ" to "JWT")
            val claims = mutableMapOf<String, Any>(
                "sub" to userId.toString(),
                "exp" to expirationDate.time / 1000,  // seconds
                "iat" to issuedDate.time / 1000       // seconds
            )

            // Optional claims
            email?.let { claims["email"] = it }
            name?.let { claims["name"] = it }

            return Jwt(
                token,
                Instant.ofEpochSecond(issuedDate.time / 1000),
                Instant.ofEpochSecond(expirationDate.time / 1000),
                headers,
                claims
            )

        } catch (e: Exception) {
            throw JwtException("Failed to decode JWT token", e)
        }
    }
}
