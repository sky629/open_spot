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
 */
data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long
) {
    companion object {
        fun from(refreshResult: TokenRefreshUseCase.TokenRefreshResult): TokenRefreshResponse {
            return TokenRefreshResponse(
                accessToken = refreshResult.accessToken,
                refreshToken = refreshResult.refreshToken,
                accessTokenExpiresIn = refreshResult.accessTokenExpiresIn
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