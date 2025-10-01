package com.kangpark.openspot.auth.service

import com.kangpark.openspot.auth.domain.entity.User
import com.kangpark.openspot.auth.service.usecase.*
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * 인증 애플리케이션 서비스
 * Use Case들을 조합하여 애플리케이션 비즈니스 플로우를 관리
 */
@Service
class AuthApplicationService(
    private val loginUseCase: LoginUseCase,
    private val tokenRefreshUseCase: TokenRefreshUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) {
    
    /**
     * Google OAuth2 로그인
     */
    fun login(oauth2User: OAuth2User): LoginUseCase.LoginResult {
        return loginUseCase.execute(oauth2User)
    }
    
    /**
     * 토큰 갱신
     */
    fun refreshToken(refreshToken: String): TokenRefreshUseCase.TokenRefreshResult {
        return tokenRefreshUseCase.execute(refreshToken)
    }
    
    /**
     * 로그아웃 (액세스 토큰 기반)
     */
    fun logout(accessToken: String) {
        logoutUseCase.execute(accessToken)
    }
    
    /**
     * 로그아웃 (사용자 ID 기반)
     */
    fun logout(userId: UUID) {
        logoutUseCase.execute(userId)
    }
    
    /**
     * 특정 리프레시 토큰으로 로그아웃
     */
    fun logoutWithRefreshToken(refreshToken: String) {
        logoutUseCase.executeWithRefreshToken(refreshToken)
    }
    
    /**
     * 현재 사용자 프로필 조회 (액세스 토큰 기반)
     */
    fun getCurrentUserProfile(accessToken: String): User {
        return getUserProfileUseCase.execute(accessToken)
    }
    
    /**
     * 사용자 프로필 조회 (사용자 ID 기반)
     */
    fun getUserProfile(userId: UUID): User {
        return getUserProfileUseCase.execute(userId)
    }
    
    /**
     * Google ID로 사용자 프로필 조회
     */
    fun getUserProfileBySocialId(socialId: String): User {
        return getUserProfileUseCase.executeBySocialId(socialId)
    }
    
    /**
     * 이메일로 사용자 프로필 조회
     */
    fun getUserProfileByEmail(email: String): User {
        return getUserProfileUseCase.executeByEmail(email)
    }
}