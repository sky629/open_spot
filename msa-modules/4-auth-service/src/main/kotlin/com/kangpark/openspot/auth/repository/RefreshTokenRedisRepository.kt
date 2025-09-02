package com.kangpark.openspot.auth.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.kangpark.openspot.auth.domain.RefreshToken
import com.kangpark.openspot.auth.domain.RefreshTokenRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * RefreshToken Redis Repository 구현체
 * Redis를 활용한 TTL 기반 리프레시 토큰 관리
 */
@Repository
class RefreshTokenRedisRepository(
    private val redisTemplate: StringRedisTemplate
) : RefreshTokenRepository {
    
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
    }
    
    /**
     * Redis 키 네임스페이스
     */
    private companion object {
        const val TOKEN_KEY_PREFIX = "refresh_token:"
        const val USER_TOKENS_KEY_PREFIX = "user_tokens:"
    }
    
    override fun save(refreshToken: RefreshToken): RefreshToken {
        val tokenKey = TOKEN_KEY_PREFIX + refreshToken.token
        val userKey = USER_TOKENS_KEY_PREFIX + refreshToken.userId
        
        // 기존 사용자 토큰 삭제 (단일 세션 정책)
        deleteByUserId(refreshToken.userId)
        
        // 토큰 정보를 JSON으로 직렬화하여 저장
        val tokenJson = objectMapper.writeValueAsString(refreshToken)
        
        // TTL과 함께 토큰 저장
        val ttlSeconds = RefreshToken.getTtlSeconds()
        redisTemplate.opsForValue().set(tokenKey, tokenJson, ttlSeconds, TimeUnit.SECONDS)
        
        // 사용자별 토큰 목록에도 추가 (사용자 ID로 조회 가능하도록)
        redisTemplate.opsForSet().add(userKey, refreshToken.token)
        redisTemplate.expire(userKey, Duration.ofSeconds(ttlSeconds))
        
        return refreshToken
    }
    
    override fun findByToken(token: String): RefreshToken? {
        val tokenKey = TOKEN_KEY_PREFIX + token
        val tokenJson = redisTemplate.opsForValue().get(tokenKey) ?: return null
        
        return try {
            objectMapper.readValue(tokenJson, RefreshToken::class.java)
        } catch (e: Exception) {
            // JSON 파싱 실패 시 null 반환하고 키 삭제
            redisTemplate.delete(tokenKey)
            null
        }
    }
    
    override fun findByUserId(userId: UUID): List<RefreshToken> {
        val userKey = USER_TOKENS_KEY_PREFIX + userId
        val tokenSet = redisTemplate.opsForSet().members(userKey) ?: return emptyList()
        
        return tokenSet.mapNotNull { token: String ->
            findByToken(token)
        }
    }
    
    override fun deleteByToken(token: String) {
        val tokenKey = TOKEN_KEY_PREFIX + token
        
        // 먼저 토큰 정보를 가져와서 사용자 ID 확인
        val refreshToken = findByToken(token)
        if (refreshToken != null) {
            val userKey = USER_TOKENS_KEY_PREFIX + refreshToken.userId
            redisTemplate.opsForSet().remove(userKey, token)
        }
        
        // 토큰 삭제
        redisTemplate.delete(tokenKey)
    }
    
    override fun deleteByUserId(userId: UUID) {
        val userKey = USER_TOKENS_KEY_PREFIX + userId
        
        // 사용자의 모든 토큰 조회
        val tokens = redisTemplate.opsForSet().members(userKey) ?: return
        
        // 각 토큰 삭제
        tokens.forEach { token: String ->
            val tokenKey = TOKEN_KEY_PREFIX + token
            redisTemplate.delete(tokenKey)
        }
        
        // 사용자 토큰 목록 삭제
        redisTemplate.delete(userKey)
    }
    
    override fun deleteExpiredTokens() {
        // Redis TTL을 사용하므로 만료된 토큰은 자동으로 삭제됨
        // 이 메서드는 Redis 환경에서는 필요 없지만 인터페이스 호환성을 위해 유지
        
        // 필요하다면 정리 작업을 위한 스캔 로직을 추가할 수 있음
        // 현재는 Redis의 TTL 자동 만료 기능에 의존
    }
    
    /**
     * Redis 키 스캔을 통한 통계 조회 (관리용)
     */
    fun getActiveTokenCount(): Long {
        val keys = redisTemplate.keys("${TOKEN_KEY_PREFIX}*")
        return keys?.size?.toLong() ?: 0L
    }
    
    /**
     * 특정 사용자의 토큰 개수 조회 (관리용)
     */
    fun getUserTokenCount(userId: UUID): Long {
        val userKey = USER_TOKENS_KEY_PREFIX + userId
        return redisTemplate.opsForSet().size(userKey) ?: 0L
    }
}