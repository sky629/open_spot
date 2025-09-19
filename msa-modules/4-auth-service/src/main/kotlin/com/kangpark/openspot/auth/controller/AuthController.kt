package com.kangpark.openspot.auth.controller

import com.kangpark.openspot.auth.controller.dto.request.TokenRefreshRequest
import com.kangpark.openspot.auth.controller.dto.response.LogoutResponse
import com.kangpark.openspot.auth.controller.dto.response.TokenRefreshResponse
import com.kangpark.openspot.auth.service.AuthApplicationService
import com.kangpark.openspot.auth.service.usecase.TokenRefreshUseCase
import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 컨트롤러
 * Google OAuth2 로그인, 토큰 갱신, 로그아웃 API 제공
 */
@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authApplicationService: AuthApplicationService
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    /**
     * Google OAuth2 로그인 시작
     * 실제 로그인은 OAuth2LoginSuccessHandler에서 처리됨
     */
    @Operation(
        summary = "Google OAuth2 로그인",
        description = "Google OAuth2를 통한 사용자 인증을 시작합니다. 성공 시 JWT 토큰이 발급됩니다."
    )
    @GetMapping("/google/login")
    fun login(response: HttpServletResponse) {
        response.sendRedirect("/oauth2/authorization/google")
    }
    
    /**
     * JWT 토큰 갱신
     */
    @Operation(
        summary = "JWT 토큰 갱신",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다."
    )
    @PostMapping("/token/refresh")
    fun refreshToken(
        @Valid @RequestBody request: TokenRefreshRequest
    ): ResponseEntity<ApiResponse<TokenRefreshResponse>> {
        return try {
            val refreshResult = authApplicationService.refreshToken(request.refreshToken)
            val response = TokenRefreshResponse.from(refreshResult)
            
            logger.info("Token refresh successful for refresh token")
            ResponseEntity.ok(ApiResponse.success(response))
            
        } catch (e: TokenRefreshUseCase.TokenRefreshException) {
            logger.warn("Token refresh failed: {}", e.message)
            val errorResponse = ApiResponse.error<TokenRefreshResponse>(
                ErrorResponse(
                    code = "TOKEN_REFRESH_FAILED",
                    message = e.message ?: "토큰 갱신에 실패했습니다"
                )
            )
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
            
        } catch (e: Exception) {
            logger.error("Unexpected error during token refresh", e)
            val errorResponse = ApiResponse.error<TokenRefreshResponse>(
                ErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "서버 오류가 발생했습니다"
                )
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
    
    /**
     * 로그아웃
     */
    @Operation(
        summary = "로그아웃",
        description = "현재 사용자의 리프레시 토큰을 무효화하여 로그아웃합니다."
    )
    @PostMapping("/logout")
    fun logout(
        @Parameter(description = "Authorization 헤더의 Bearer 토큰")
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<ApiResponse<LogoutResponse>> {
        return try {
            val accessToken = extractTokenFromAuthorizationHeader(authorization)
            authApplicationService.logout(accessToken)
            
            logger.info("Logout successful")
            ResponseEntity.ok(ApiResponse.success(LogoutResponse()))
            
        } catch (e: IllegalArgumentException) {
            logger.warn("Logout failed - invalid token: {}", e.message)
            val errorResponse = ApiResponse.error<LogoutResponse>(
                ErrorResponse(
                    code = "INVALID_TOKEN",
                    message = "유효하지 않은 토큰입니다"
                )
            )
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
            
        } catch (e: Exception) {
            logger.error("Unexpected error during logout", e)
            val errorResponse = ApiResponse.error<LogoutResponse>(
                ErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "서버 오류가 발생했습니다"
                )
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
    
    /**
     * Authorization 헤더에서 토큰 추출
     */
    private fun extractTokenFromAuthorizationHeader(authorization: String): String {
        if (!authorization.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid Authorization header format")
        }
        return authorization.substring(7)
    }
}