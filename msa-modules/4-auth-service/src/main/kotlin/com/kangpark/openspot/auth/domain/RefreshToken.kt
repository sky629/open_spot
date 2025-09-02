package com.kangpark.openspot.auth.domain

import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * JWT 리프레시 토큰 도메인 엔터티
 * Redis TTL 기반 만료 관리
 */
class RefreshToken(
    val userId: UUID,
    val token: String,
    val expiresAt: LocalDateTime
) {
    
    companion object {
        const val REFRESH_TOKEN_VALIDITY_DAYS = 30L
        
        fun create(userId: UUID, token: String): RefreshToken {
            require(token.isNotBlank()) { "리프레시 토큰은 필수입니다" }
            
            return RefreshToken(
                userId = userId,
                token = token,
                expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)
            )
        }
        
        /**
         * Redis TTL 계산 (초 단위)
         */
        fun getTtlSeconds(): Long {
            return Duration.ofDays(REFRESH_TOKEN_VALIDITY_DAYS).seconds
        }
    }
    
    /**
     * Redis 키 생성 (토큰 기반)
     */
    fun getRedisKey(): String {
        return "refresh_token:$token"
    }
    
    /**
     * 사용자별 Redis 키 생성
     */
    fun getUserRedisKey(): String {
        return "user_tokens:$userId"
    }
    
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
    
    fun isValid(): Boolean {
        return !isExpired()
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RefreshToken) return false
        return token == other.token
    }
    
    override fun hashCode(): Int {
        return token.hashCode()
    }
    
    override fun toString(): String {
        return "RefreshToken(userId=$userId, expiresAt=$expiresAt)"
    }
}