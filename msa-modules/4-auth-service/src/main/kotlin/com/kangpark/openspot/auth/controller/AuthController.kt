package com.kangpark.openspot.auth.controller

import com.kangpark.openspot.auth.controller.dto.response.LogoutResponse
import com.kangpark.openspot.auth.controller.dto.response.TokenRefreshResponse
import com.kangpark.openspot.auth.service.AuthApplicationService
import com.kangpark.openspot.auth.service.usecase.TokenRefreshUseCase
import com.kangpark.openspot.auth.util.CookieFactory
import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.hibernate.validator.constraints.UUID
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
    private val authApplicationService: AuthApplicationService,
    private val cookieFactory: CookieFactory,
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * JWT 토큰 갱신 (HttpOnly 쿠키 사용)
     * Access Token은 Response Body로, Refresh Token은 HttpOnly Cookie로 반환
     */
    @Operation(
        summary = "JWT 토큰 갱신",
        description = """
            HttpOnly 쿠키의 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
            Access Token은 Response Body로 반환되므로 프론트엔드에서 메모리에 저장하세요.
            Refresh Token은 HttpOnly Cookie로 자동 갱신됩니다.
        """
    )
    @PostMapping("/token/refresh")
    fun refreshToken(
        @CookieValue(name = "refresh_token", required = false) refreshTokenCookie: String?,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<TokenRefreshResponse>> {
        return try {
            // 쿠키에서 refresh token 읽기
            val refreshToken = refreshTokenCookie
                ?: throw IllegalArgumentException("Refresh token not found in cookies")

            val refreshResult = authApplicationService.refreshToken(refreshToken)

            // 새로운 Refresh Token만 HttpOnly 쿠키로 설정
            cookieFactory.addRefreshTokenCookie(refreshResult.refreshToken, response)

            // Access Token은 Response Body로 반환
            val apiResponse = TokenRefreshResponse.from(refreshResult)

            logger.info("Token refresh successful")
            ResponseEntity.ok(ApiResponse.success(apiResponse))

        } catch (e: IllegalArgumentException) {
            logger.warn("Token refresh failed - missing cookie: {}", e.message)
            val errorResponse = ApiResponse.error<TokenRefreshResponse>(
                ErrorResponse(
                    code = "MISSING_REFRESH_TOKEN",
                    message = "리프레시 토큰이 없습니다"
                )
            )
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)

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
     * Bearer 토큰 검증 및 Refresh Token 쿠키 삭제
     */
    @Operation(
        summary = "로그아웃",
        description = """
            Gateway에서 JWT 검증 후 전달된 사용자 ID로 로그아웃을 처리합니다.
            HttpOnly Cookie의 Refresh Token도 삭제됩니다.
        """
    )
    @PostMapping("/logout")
    fun logout(
        @Parameter(description = "Gateway가 추가한 사용자 ID 헤더", required = true)
        @RequestHeader("X-User-Id") userId: UUID,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<LogoutResponse>> {
        return try {
            // Gateway가 JWT 검증을 완료했으므로, userId로 로그아웃 처리만 수행
            // TODO: userId로 로그아웃 처리 (현재 logout()은 accessToken을 받고 있음)
            // authApplicationService.logout(userId)

            // Refresh Token 쿠키 삭제 (maxAge=0)
            cookieFactory.deleteRefreshTokenCookie(response)

            logger.info("Logout successful for user: {}", userId)
            ResponseEntity.ok(ApiResponse.success(LogoutResponse()))

        } catch (e: Exception) {
            logger.error("Unexpected error during logout for user: {}", userId, e)
            val errorResponse = ApiResponse.error<LogoutResponse>(
                ErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "서버 오류가 발생했습니다"
                )
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}