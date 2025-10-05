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
     * Gateway에서 전달한 X-User-Id 헤더로 프로필 정보를 조회합니다.
     */
    @Operation(
        summary = "현재 사용자 프로필 조회",
        description = "Gateway에서 전달한 X-User-Id 헤더를 통해 프로필 정보를 조회합니다."
    )
    @GetMapping("/self")
    fun getCurrentUser(
        @Parameter(description = "사용자 ID (Gateway에서 자동 주입)", hidden = true)
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ApiResponse<UserResponse>> {
        return try {
            val userUuid = UUID.fromString(userId)
            val user = authApplicationService.getUserProfile(userUuid)
            val response = UserResponse.from(user)

            logger.info("Current user profile retrieved: {} (ID: {})", user.email, userUuid)
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
}