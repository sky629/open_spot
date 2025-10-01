package com.kangpark.openspot.auth.domain.repository

import com.kangpark.openspot.auth.domain.entity.RefreshToken
import java.util.UUID

/**
 * 리프레시 토큰 리포지토리 인터페이스
 * Domain Layer에서 정의하여 Infrastructure Layer에서 구현
 */
interface RefreshTokenRepository {
    
    /**
     * 리프레시 토큰 저장
     */
    fun save(refreshToken: RefreshToken): RefreshToken
    
    /**
     * 토큰으로 조회
     */
    fun findByToken(token: String): RefreshToken?
    
    /**
     * 사용자 ID로 조회
     */
    fun findByUserId(userId: UUID): List<RefreshToken>
    
    /**
     * 토큰으로 삭제
     */
    fun deleteByToken(token: String)
    
    /**
     * 사용자의 모든 리프레시 토큰 삭제
     */
    fun deleteByUserId(userId: UUID)
    
    /**
     * 만료된 토큰 삭제
     */
    fun deleteExpiredTokens()
}