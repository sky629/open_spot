package com.kangpark.openspot.auth.controller.dto.response

import com.kangpark.openspot.auth.service.usecase.LoginUseCase
import com.kangpark.openspot.auth.service.usecase.TokenRefreshUseCase

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val isNewUser: Boolean
) {
    companion object {
        fun from(loginResult: LoginUseCase.LoginResult): LoginResponse {
            return LoginResponse(
                user = UserResponse.from(loginResult.user),
                accessToken = loginResult.accessToken,
                refreshToken = loginResult.refreshToken,
                accessTokenExpiresIn = loginResult.accessTokenExpiresIn,
                isNewUser = loginResult.isNewUser
            )
        }
    }
}

/**
 * 토큰 갱신 응답 DTO
 * Access Token은 Response Body로 전달 (프론트엔드 메모리 저장)
 * Refresh Token은 HttpOnly Cookie로 전달 (XSS 방어)
 */
data class TokenRefreshResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val message: String = "Token refreshed successfully"
) {
    companion object {
        fun from(refreshResult: TokenRefreshUseCase.TokenRefreshResult): TokenRefreshResponse {
            return TokenRefreshResponse(
                accessToken = refreshResult.accessToken,
                accessTokenExpiresIn = refreshResult.accessTokenExpiresIn,
                message = "Token refreshed successfully"
            )
        }
    }
}

/**
 * 로그아웃 응답 DTO
 */
data class LogoutResponse(
    val message: String = "Successfully logged out"
)