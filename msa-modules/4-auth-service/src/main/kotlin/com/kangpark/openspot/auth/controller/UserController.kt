package com.kangpark.openspot.auth.controller

import com.kangpark.openspot.auth.controller.dto.response.UserResponse
import com.kangpark.openspot.auth.service.AuthApplicationService
import com.kangpark.openspot.auth.service.usecase.GetUserProfileUseCase
import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 사용자 컨트롤러
 * 사용자 프로필 관련 API 제공
 */
@Tag(name = "Users", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val authApplicationService: AuthApplicationService
) {
    
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    /**
     * 현재 사용자 프로필 조회
     */
    @Operation(
        summary = "현재 사용자 프로필 조회",
        description = "JWT 토큰을 통해 현재 로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @GetMapping("/self")
    fun getCurrentUser(
        @Parameter(description = "Authorization 헤더의 Bearer 토큰")
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<ApiResponse<UserResponse>> {
        return try {
            val accessToken = extractTokenFromAuthorizationHeader(authorization)
            val user = authApplicationService.getCurrentUserProfile(accessToken)
            val response = UserResponse.from(user)
            
            logger.info("Current user profile retrieved: {}", user.email)
            ResponseEntity.ok(ApiResponse.success(response))
            
        } catch (e: GetUserProfileUseCase.UserProfileException) {
            logger.warn("User profile retrieval failed: {}", e.message)
            val errorResponse = ApiResponse.error<UserResponse>(
                ErrorResponse(
                    code = "USER_NOT_FOUND",
                    message = e.message ?: "사용자를 찾을 수 없습니다"
                )
            )
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
            
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid token for user profile: {}", e.message)
            val errorResponse = ApiResponse.error<UserResponse>(
                ErrorResponse(
                    code = "INVALID_TOKEN",
                    message = "유효하지 않은 토큰입니다"
                )
            )
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
            
        } catch (e: Exception) {
            logger.error("Unexpected error during user profile retrieval", e)
            val errorResponse = ApiResponse.error<UserResponse>(
                ErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "서버 오류가 발생했습니다"
                )
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
    
    /**
     * 사용자 ID로 프로필 조회 (관리자용)
     */
    @Operation(
        summary = "사용자 프로필 조회",
        description = "사용자 ID를 통해 특정 사용자의 프로필 정보를 조회합니다. (관리자용)"
    )
    @GetMapping("/{userId}")
    fun getUserProfile(
        @Parameter(description = "사용자 ID")
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<UserResponse>> {
        return try {
            val user = authApplicationService.getUserProfile(userId)
            val response = UserResponse.from(user)
            
            logger.info("User profile retrieved for ID: {}", userId)
            ResponseEntity.ok(ApiResponse.success(response))
            
        } catch (e: GetUserProfileUseCase.UserProfileException) {
            logger.warn("User profile retrieval failed for ID {}: {}", userId, e.message)
            val errorResponse = ApiResponse.error<UserResponse>(
                ErrorResponse(
                    code = "USER_NOT_FOUND",
                    message = "사용자를 찾을 수 없습니다"
                )
            )
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
            
        } catch (e: Exception) {
            logger.error("Unexpected error during user profile retrieval for ID: {}", userId, e)
            val errorResponse = ApiResponse.error<UserResponse>(
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