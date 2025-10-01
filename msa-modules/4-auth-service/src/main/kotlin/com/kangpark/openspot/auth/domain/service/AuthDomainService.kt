package com.kangpark.openspot.auth.domain.service

import com.kangpark.openspot.auth.domain.entity.RefreshToken
import com.kangpark.openspot.auth.domain.entity.SocialAccount
import com.kangpark.openspot.auth.domain.entity.User
import com.kangpark.openspot.auth.domain.repository.RefreshTokenRepository
import com.kangpark.openspot.auth.domain.repository.SocialAccountRepository
import com.kangpark.openspot.auth.domain.repository.UserRepository
import com.kangpark.openspot.auth.domain.vo.SocialProvider
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

/**
 * 인증 도메인 서비스
 * 복잡한 비즈니스 로직을 처리
 */
@Service
class AuthDomainService(
    val userRepository: UserRepository,
    val refreshTokenRepository: RefreshTokenRepository,
    val socialAccountRepository: SocialAccountRepository
) {

    /**
     * Google OAuth2 정보로 사용자 등록
     */
    fun registerUser(
        socialId: String,
        email: String,
        name: String,
        pictureUrl: String?
    ): User {
        val newUser = User.create(socialId, email, name, pictureUrl)
        return userRepository.save(newUser)
    }

    /**
     * Google OAuth2 정보로 사용자 업데이트
     */
    fun updateUser(
        socialId: String,
        name: String,
        pictureUrl: String?
    ): User? {
        val existingUser = userRepository.findBySocialId(socialId)
        if (existingUser != null) {
            val updatedUser = existingUser.updateProfile(name, pictureUrl)
            return userRepository.save(updatedUser)
        }
        return null
    }
    
    /**
     * 리프레시 토큰 생성 및 저장
     */
    fun createRefreshToken(userId: java.util.UUID, token: String): RefreshToken {
        // 기존 리프레시 토큰 삭제 (단일 세션 정책)
        refreshTokenRepository.deleteByUserId(userId)
        
        val refreshToken = RefreshToken.create(userId, token)
        return refreshTokenRepository.save(refreshToken)
    }
    
    /**
     * 리프레시 토큰 검증 및 갱신
     */
    fun validateAndRefreshToken(token: String): RefreshToken? {
        val refreshToken = refreshTokenRepository.findByToken(token)
            ?: return null
            
        if (refreshToken.isExpired()) {
            refreshTokenRepository.deleteByToken(token)
            return null
        }
        
        return refreshToken
    }
    
    /**
     * 사용자 로그아웃 (리프레시 토큰 삭제)
     */
    fun logout(userId: java.util.UUID) {
        refreshTokenRepository.deleteByUserId(userId)
    }
    
    /**
     * 만료된 리프레시 토큰 정리
     */
    fun cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens()
    }

    /**
     * 소셜 계정 등록
     */
    fun registerSocialAccount(
        userId: UUID,
        provider: SocialProvider,
        providerId: String,
        email: String,
        displayName: String,
        profileImageUrl: String?
    ): SocialAccount {
        val newSocialAccount = SocialAccount.create(
            userId = userId,
            provider = provider,
            providerId = providerId,
            email = email,
            displayName = displayName,
            profileImageUrl = profileImageUrl,
            connectedAt = LocalDateTime.now()
        )
        return socialAccountRepository.save(newSocialAccount)
    }

    /**
     * 소셜 계정 등록 또는 업데이트
     */
    fun updateSocialAccount(
        userId: UUID,
        provider: SocialProvider,
        email: String,
        displayName: String,
        profileImageUrl: String?
    ): SocialAccount? {
        val existingSocialAccount = socialAccountRepository.findByUserIdAndProvider(userId, provider)

        if (existingSocialAccount != null) {
            // 기존 소셜 계정 정보 업데이트
            val updatedSocialAccount = existingSocialAccount.updateInfo(email, displayName, profileImageUrl)
            return socialAccountRepository.save(updatedSocialAccount)
        }
        return null
    }
    
    /**
     * 사용자의 모든 소셜 계정 조회
     */
    fun getUserSocialAccounts(userId: UUID): List<SocialAccount> {
        return socialAccountRepository.findByUserId(userId)
    }
    
    /**
     * 소셜 계정으로 사용자 조회
     */
    fun findUserBySocialAccount(provider: SocialProvider, providerId: String): User? {
        val socialAccount = socialAccountRepository.findByProviderAndProviderId(provider, providerId)
            ?: return null
        
        return userRepository.findById(socialAccount.userId)
    }
    
    /**
     * 소셜 계정 연결 해제
     */
    fun disconnectSocialAccount(userId: UUID, provider: SocialProvider) {
        socialAccountRepository.findByUserIdAndProvider(userId, provider)
            ?.let { socialAccountRepository.deleteById(it.id) }
    }
    
    /**
     * 사용자 삭제 시 모든 소셜 계정 삭제
     */
    fun deleteUserSocialAccounts(userId: UUID) {
        socialAccountRepository.deleteByUserId(userId)
    }
}