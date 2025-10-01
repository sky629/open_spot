package com.kangpark.openspot.auth.service.usecase

import com.kangpark.openspot.auth.domain.service.AuthDomainService
import com.kangpark.openspot.auth.domain.repository.UserRepository
import com.kangpark.openspot.auth.repository.external.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 토큰 갱신 Use Case
 * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
 */
@Service
@Transactional
class TokenRefreshUseCase(
    private val authDomainService: AuthDomainService,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    /**
     * 토큰 갱신 결과
     */
    data class TokenRefreshResult(
        val accessToken: String,
        val refreshToken: String,
        val accessTokenExpiresIn: Long
    )
    
    /**
     * 토큰 갱신 실행
     */
    fun execute(refreshToken: String): TokenRefreshResult {
        // 1. 리프레시 토큰 유효성 검증
        val validRefreshToken = authDomainService.validateAndRefreshToken(refreshToken)
            ?: throw TokenRefreshException("Invalid or expired refresh token")
        
        // 2. 사용자 정보 조회
        val user = userRepository.findById(validRefreshToken.userId)
            ?: throw TokenRefreshException("User not found for refresh token")
        
        // 3. 새로운 액세스 토큰 생성
        val newAccessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id,
            socialId = user.socialId,
            email = user.email,
            name = user.name
        )
        
        // 4. 새로운 리프레시 토큰 생성 (선택사항: 리프레시 토큰 로테이션)
        val newRefreshTokenValue = jwtTokenProvider.generateRefreshToken(user.id)
        authDomainService.createRefreshToken(user.id, newRefreshTokenValue)
        
        return TokenRefreshResult(
            accessToken = newAccessToken,
            refreshToken = newRefreshTokenValue,
            accessTokenExpiresIn = getAccessTokenExpirationMs()
        )
    }
    
    /**
     * 액세스 토큰 만료 시간(밀리초) 조회
     */
    private fun getAccessTokenExpirationMs(): Long {
        return 24 * 60 * 60 * 1000 // 24시간
    }
    
    /**
     * 토큰 갱신 예외
     */
    class TokenRefreshException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
}