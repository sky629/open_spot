package com.kangpark.openspot.auth.service.usecase

import com.kangpark.openspot.auth.domain.service.AuthDomainService
import com.kangpark.openspot.auth.domain.vo.SocialProvider
import com.kangpark.openspot.auth.domain.entity.User
import com.kangpark.openspot.auth.repository.external.GoogleOAuthClient
import com.kangpark.openspot.auth.repository.external.JwtTokenProvider
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 로그인 Use Case
 * Google OAuth2를 통한 사용자 인증 및 JWT 토큰 발급
 */
@Service
@Transactional
class LoginUseCase(
    private val authDomainService: AuthDomainService,
    private val googleOAuthClient: GoogleOAuthClient,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    /**
     * 로그인 결과
     */
    data class LoginResult(
        val user: User,
        val accessToken: String,
        val refreshToken: String,
        val accessTokenExpiresIn: Long,
        val isNewUser: Boolean
    )
    
    /**
     * Google OAuth2 로그인 실행
     */
    fun execute(oauth2User: OAuth2User): LoginResult {
        // 1. Google OAuth2User에서 사용자 정보 추출
        val googleUserInfo = googleOAuthClient.extractUserInfoSync(oauth2User)
        
        // 2. 사용자 등록 또는 업데이트 (Domain Service 사용)
        var user: User? = authDomainService.findUserBySocialAccount(SocialProvider.GOOGLE, googleUserInfo.socialId)
        val isNewUser: Boolean = (user == null)
        if (user == null) {
            user = authDomainService.registerUser(
                socialId = googleUserInfo.socialId,
                email = googleUserInfo.email,
                name = googleUserInfo.name,
                pictureUrl = googleUserInfo.pictureUrl
            )

            // 3. 소셜 계정 정보 등록 또는 업데이트
            authDomainService.registerSocialAccount(
                userId = user.id,
                provider = SocialProvider.GOOGLE,
                providerId = googleUserInfo.socialId,
                email = googleUserInfo.email,
                displayName = googleUserInfo.name,
                profileImageUrl = googleUserInfo.pictureUrl
            )
        }

        // 4. JWT 액세스 토큰 생성
        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id,
            socialId = user.socialId,
            email = user.email,
            name = user.name
        )
        
        // 5. 리프레시 토큰 생성 및 저장
        val refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.id)
        authDomainService.createRefreshToken(user.id, refreshTokenValue)
        
        return LoginResult(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshTokenValue,
            accessTokenExpiresIn = getAccessTokenExpirationMs(),
            isNewUser = isNewUser
        )
    }
    
    /**
     * 액세스 토큰 만료 시간(밀리초) 조회
     */
    private fun getAccessTokenExpirationMs(): Long {
        // JWT 설정에서 만료 시간을 가져올 수 있지만, 현재는 기본값 사용
        return 24 * 60 * 60 * 1000 // 24시간
    }
}