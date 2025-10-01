package com.kangpark.openspot.auth.service.usecase

import com.kangpark.openspot.auth.domain.entity.User
import com.kangpark.openspot.auth.domain.repository.UserRepository
import com.kangpark.openspot.auth.repository.external.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 사용자 프로필 조회 Use Case
 */
@Service
@Transactional(readOnly = true)
class GetUserProfileUseCase(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    /**
     * 액세스 토큰으로 사용자 프로필 조회
     */
    fun execute(accessToken: String): User {
        // 1. 액세스 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw UserProfileException("Invalid access token")
        }
        
        // 2. 토큰에서 사용자 ID 추출
        val userId = jwtTokenProvider.getUserIdFromToken(accessToken)
            ?: throw UserProfileException("Unable to extract user ID from token")
        
        // 3. 사용자 조회
        return userRepository.findById(userId)
            ?: throw UserProfileException("User not found")
    }
    
    /**
     * 사용자 ID로 프로필 조회
     */
    fun execute(userId: UUID): User {
        return userRepository.findById(userId)
            ?: throw UserProfileException("User not found")
    }
    
    /**
     * Social ID로 프로필 조회
     */
    fun executeBySocialId(socialId: String): User {
        return userRepository.findBySocialId(socialId)
            ?: throw UserProfileException("User not found with Social ID")
    }
    
    /**
     * 이메일로 프로필 조회
     */
    fun executeByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw UserProfileException("User not found with email")
    }
    
    /**
     * 사용자 프로필 예외
     */
    class UserProfileException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
}