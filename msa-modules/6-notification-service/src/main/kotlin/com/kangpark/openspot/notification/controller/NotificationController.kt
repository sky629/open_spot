package com.kangpark.openspot.notification.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.ErrorResponse
import com.kangpark.openspot.common.web.dto.PageResponse
import com.kangpark.openspot.notification.controller.dto.*
import com.kangpark.openspot.notification.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Notification", description = "알림 관리 API")
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(NotificationController::class.java)

    @Operation(summary = "디바이스 토큰 등록", description = "FCM 푸시 알림을 위한 디바이스 토큰을 등록합니다.")
    @PostMapping("/tokens")
    fun registerToken(
        @Valid @RequestBody request: RegisterTokenRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<RegisterTokenResponse> {
        val userUuid = UUID.fromString(userId)

        logger.info("Registering device token for user: {}, deviceType: {}", userUuid, request.deviceType)

        val deviceToken = notificationService.registerDeviceToken(
            userId = userUuid,
            token = request.token,
            deviceType = request.deviceType,
            deviceId = request.deviceId
        )

        val response = RegisterTokenResponse(
            id = deviceToken.id!!,
            token = deviceToken.token,
            deviceType = deviceToken.deviceType,
            isActive = deviceToken.isActive,
            createdAt = deviceToken.createdAt
        )

        return ApiResponse.success(response)
    }

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 페이지네이션으로 조회합니다.")
    @GetMapping
    fun getNotifications(
        @RequestHeader("X-User-Id") userId: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<PageResponse<NotificationResponse>> {
        val userUuid = UUID.fromString(userId)

        logger.debug("Getting notifications for user: {}, page: {}, size: {}",
            userUuid, pageable.pageNumber, pageable.pageSize)

        val notificationPage = notificationService.getUserNotifications(userUuid, pageable)
        val pageResponse = PageResponse.from(notificationPage.map { NotificationResponse.from(it) })

        return ApiResponse.success(pageResponse)
    }

    @Operation(summary = "읽지 않은 알림 수 조회", description = "사용자의 읽지 않은 알림 개수를 조회합니다.")
    @GetMapping("/unread-count")
    fun getUnreadCount(
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<UnreadCountResponse> {
        val userUuid = UUID.fromString(userId)

        val unreadCount = notificationService.getUnreadNotificationCount(userUuid)

        logger.debug("Unread notification count for user {}: {}", userUuid, unreadCount)

        return ApiResponse.success(UnreadCountResponse(unreadCount))
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PutMapping("/{notificationId}/read")
    fun markAsRead(
        @PathVariable notificationId: UUID,
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<Unit> {
        val userUuid = UUID.fromString(userId)

        logger.info("Marking notification as read: notificationId={}, userId={}", notificationId, userUuid)

        val success = notificationService.markNotificationAsRead(userUuid, notificationId)

        return if (success) {
            ApiResponse.success(Unit)
        } else {
            ApiResponse.error(ErrorResponse("NOT_FOUND", "알림을 찾을 수 없습니다"))
        }
    }

    @Operation(summary = "알림 설정 조회", description = "사용자의 알림 설정을 조회합니다.")
    @GetMapping("/settings")
    fun getNotificationSettings(
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<NotificationSettingsResponse> {
        val userUuid = UUID.fromString(userId)

        logger.debug("Getting notification settings for user: {}", userUuid)

        val settings = notificationService.getNotificationSettings(userUuid)
        val response = NotificationSettingsResponse.from(settings)

        return ApiResponse.success(response)
    }

    @Operation(summary = "알림 설정 업데이트", description = "사용자의 알림 설정을 업데이트합니다.")
    @PutMapping("/settings")
    fun updateNotificationSettings(
        @Valid @RequestBody request: UpdateSettingsRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<NotificationSettingsResponse> {
        val userUuid = UUID.fromString(userId)

        logger.info("Updating notification settings for user: {}, reportEnabled: {}, systemEnabled: {}",
            userUuid, request.reportEnabled, request.systemEnabled)

        val updatedSettings = notificationService.updateNotificationSettings(
            userId = userUuid,
            reportEnabled = request.reportEnabled,
            systemEnabled = request.systemEnabled
        )

        val response = NotificationSettingsResponse.from(updatedSettings)

        return ApiResponse.success(response)
    }
}