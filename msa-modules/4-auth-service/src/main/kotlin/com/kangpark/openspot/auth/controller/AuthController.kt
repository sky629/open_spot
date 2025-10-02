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
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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
    private val cookieFactory: CookieFactory
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    companion object {
        const val REDIRECT_URI_SESSION_KEY = "OAUTH2_REDIRECT_URI"
    }

    /**
     * Google OAuth2 로그인 시작
     * 실제 로그인은 OAuth2LoginSuccessHandler에서 처리됨
     */
    @Operation(
        summary = "Google OAuth2 로그인",
        description = """
            Google OAuth2를 통한 사용자 인증을 시작합니다. 성공 시 JWT 토큰이 발급됩니다.

            redirect_uri 파라미터를 통해 로그인 성공 후 리다이렉트할 URL을 지정할 수 있습니다.
            화이트리스트에 등록된 URL만 허용됩니다.
        """
    )
    @GetMapping("/google/login")
    fun login(
        @Parameter(description = "로그인 성공 후 리다이렉트할 URL (선택사항)")
        @RequestParam(name = "redirect_uri", required = false) redirectUri: String?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        // redirect_uri를 Session에 저장 (OAuth2 플로우 완료 후 사용)
        redirectUri?.let {
            request.session.setAttribute(REDIRECT_URI_SESSION_KEY, it)
            logger.debug("Stored redirect_uri in session: $it")
        }

        response.sendRedirect("/oauth2/authorization/google")
    }
    
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
            Bearer 토큰을 검증하고 Redis의 Refresh Token을 무효화합니다.
            HttpOnly Cookie의 Refresh Token도 삭제됩니다.
            Authorization: Bearer <access-token> 헤더 필요
        """
    )
    @PostMapping("/logout")
    fun logout(
        @RequestHeader(value = "Authorization", required = false) authHeader: String?,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<LogoutResponse>> {
        return try {
            // Authorization 헤더에서 Bearer 토큰 추출
            val accessToken = authHeader
                ?.takeIf { it.startsWith("Bearer ") }
                ?.substring(7)
                ?: throw IllegalArgumentException("Access token not found in Authorization header")

            authApplicationService.logout(accessToken)

            // Refresh Token 쿠키 삭제 (maxAge=0)
            cookieFactory.deleteRefreshTokenCookie(response)

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
}