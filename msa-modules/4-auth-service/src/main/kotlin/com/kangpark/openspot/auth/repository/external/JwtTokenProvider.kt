package com.kangpark.openspot.auth.repository.external

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * JWT 토큰 생성 및 검증 컴포넌트 (Spring Security OAuth2 기반)
 */
@Component
class JwtTokenProvider(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,

    @Value("\${jwt.access-token-expiration:3600000}") // 1시간 기본값
    private val accessTokenExpirationMs: Long,

    @Value("\${jwt.refresh-token-expiration:604800000}") // 7일 기본값
    private val refreshTokenExpirationMs: Long
) {

    /**
     * 사용자 정보로부터 액세스 토큰 생성
     */
    fun generateAccessToken(
        userId: UUID,
        socialId: String,
        email: String,
        name: String
    ): String {
        val now = Instant.now()
        val expiresAt = now.plus(accessTokenExpirationMs, ChronoUnit.MILLIS)

        val claims = JwtClaimsSet.builder()
            .subject(userId.toString())
            .claim("socialId", socialId)
            .claim("email", email)
            .claim("name", name)
            .claim("type", "ACCESS_TOKEN")
            .issuedAt(now)
            .expiresAt(expiresAt)
            .build()

        val jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }

    /**
     * 리프레시 토큰 생성
     */
    fun generateRefreshToken(userId: UUID): String {
        val now = Instant.now()
        val expiresAt = now.plus(refreshTokenExpirationMs, ChronoUnit.MILLIS)

        val claims = JwtClaimsSet.builder()
            .subject(userId.toString())
            .claim("type", "REFRESH_TOKEN")
            .issuedAt(now)
            .expiresAt(expiresAt)
            .build()

        val jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): UUID? {
        return try {
            val jwt = jwtDecoder.decode(token)
            UUID.fromString(jwt.subject)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 토큰에서 Social ID 추출
     */
    fun getSocialIdFromToken(token: String): String? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.getClaim<String>("socialId")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 토큰에서 이메일 추출
     */
    fun getEmailFromToken(token: String): String? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.getClaim<String>("email")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 토큰에서 이름 추출
     */
    fun getNameFromToken(token: String): String? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.getClaim<String>("name")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 토큰 발급 시간 조회
     */
    fun getIssuedAtFromToken(token: String): Date {
        return try {
            val jwt = jwtDecoder.decode(token)
            Date.from(jwt.issuedAt)
        } catch (e: Exception) {
            Date()
        }
    }

    /**
     * 토큰 타입 확인
     */
    fun getTokenType(token: String): String? {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.getClaim<String>("type")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            jwtDecoder.decode(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * 토큰 만료 여부 확인
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.expiresAt?.isBefore(Instant.now()) ?: true
        } catch (e: Exception) {
            true
        }
    }

    /**
     * 토큰 만료 시간 조회
     */
    fun getExpirationDateFromToken(token: String): Date {
        return try {
            val jwt = jwtDecoder.decode(token)
            Date.from(jwt.expiresAt)
        } catch (e: Exception) {
            Date()
        }
    }
}
