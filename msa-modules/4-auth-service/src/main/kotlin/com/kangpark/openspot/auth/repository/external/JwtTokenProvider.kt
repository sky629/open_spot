package com.kangpark.openspot.auth.repository.external

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import javax.crypto.spec.SecretKeySpec

/**
 * JWT 토큰 생성 및 검증 컴포넌트
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val jwtSecret: String,
    
    @Value("\${jwt.expiration:86400000}") // 24시간 기본값
    private val jwtExpirationMs: Long
) {
    
    private val key: Key by lazy {
        // Base64로 인코딩된 비밀키를 사용하여 HMAC SHA 키 생성
        val keyBytes = Base64.getDecoder().decode(
            Base64.getEncoder().encodeToString(jwtSecret.toByteArray())
        )
        SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.jcaName)
    }
    
    /**
     * 사용자 정보로부터 액세스 토큰 생성
     */
    fun generateAccessToken(
        userId: UUID,
        socialId: String,
        email: String,
        name: String
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("socialId", socialId)
            .claim("email", email)
            .claim("name", name)
            .claim("type", "ACCESS_TOKEN")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
    
    /**
     * 리프레시 토큰 생성
     */
    fun generateRefreshToken(userId: UUID): String {
        val now = Date()
        val expiryDate = Date(now.time + (jwtExpirationMs * 30)) // 30일
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("type", "REFRESH_TOKEN")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): UUID? {
        return try {
            val claims = getClaimsFromToken(token)
            UUID.fromString(claims.subject)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 토큰에서 Social ID 추출
     */
    fun getSocialIdFromToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims["socialId"] as? String
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 토큰에서 이메일 추출
     */
    fun getEmailFromToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims["email"] as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 토큰에서 이름 추출
     */
    fun getNameFromToken(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims["name"] as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 토큰 발급 시간 조회
     */
    fun getIssuedAtFromToken(token: String): Date {
        return try {
            getClaimsFromToken(token).issuedAt
        } catch (e: Exception) {
            Date()
        }
    }

    /**
     * 토큰 타입 확인
     */
    fun getTokenType(token: String): String? {
        return try {
            val claims = getClaimsFromToken(token)
            claims["type"] as? String
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            getClaimsFromToken(token)
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
            val claims = getClaimsFromToken(token)
            val expiration = claims.expiration
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }
    
    /**
     * 토큰에서 Claims 추출
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
    
    /**
     * 토큰 만료 시간 조회
     */
    fun getExpirationDateFromToken(token: String): Date {
        return getClaimsFromToken(token).expiration
    }
}