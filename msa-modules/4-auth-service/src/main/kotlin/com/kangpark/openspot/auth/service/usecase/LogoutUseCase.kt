package com.kangpark.openspot.auth.service.usecase

import com.kangpark.openspot.auth.domain.service.AuthDomainService
import com.kangpark.openspot.auth.repository.external.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 로그아웃 Use Case
 * 리프레시 토큰 무효화
 */
@Service
@Transactional
class LogoutUseCase(
    private val authDomainService: AuthDomainService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    /**
     * 로그아웃 실행 (Access Token 기반)
     */
    fun execute(accessToken: String) {
        // 1. 액세스 토큰에서 사용자 ID 추출
        val userId = jwtTokenProvider.getUserIdFromToken(accessToken)
            ?: throw LogoutException("Invalid access token")
        
        // 2. 사용자의 모든 리프레시 토큰 삭제 (Domain Service 사용)
        authDomainService.logout(userId)
    }
    
    /**
     * 로그아웃 실행 (User ID 기반)
     */
    fun execute(userId: UUID) {
        // 사용자의 모든 리프레시 토큰 삭제
        authDomainService.logout(userId)
    }
    
    /**
     * 특정 리프레시 토큰만 무효화
     */
    fun executeWithRefreshToken(refreshToken: String) {
        // 1. 리프레시 토큰에서 사용자 ID 추출
        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
            ?: throw LogoutException("Invalid refresh token")
        
        // 2. 해당 리프레시 토큰만 삭제
        authDomainService.refreshTokenRepository.deleteByToken(refreshToken)
    }
    
    /**
     * 로그아웃 예외
     */
    class LogoutException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
}