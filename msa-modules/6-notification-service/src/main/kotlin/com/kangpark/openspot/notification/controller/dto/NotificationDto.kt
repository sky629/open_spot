package com.kangpark.openspot.notification.controller.dto

import com.kangpark.openspot.notification.domain.DeviceType
import com.kangpark.openspot.notification.domain.Notification
import com.kangpark.openspot.notification.domain.NotificationSettings
import com.kangpark.openspot.notification.domain.NotificationType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*

// 디바이스 토큰 등록 요청
data class RegisterTokenRequest(
    @field:NotBlank(message = "토큰은 필수입니다")
    val token: String,

    @field:NotNull(message = "디바이스 타입은 필수입니다")
    val deviceType: DeviceType,

    val deviceId: String? = null
)

// 디바이스 토큰 등록 응답
data class RegisterTokenResponse(
    val id: UUID,
    val token: String,
    val deviceType: DeviceType,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)

// 알림 조회 응답
data class NotificationResponse(
    val id: UUID,
    val title: String,
    val body: String,
    val notificationType: NotificationType,
    val referenceId: UUID?,
    val isRead: Boolean,
    val isSent: Boolean,
    val createdAt: LocalDateTime,
    val readAt: LocalDateTime?
) {
    companion object {
        fun from(notification: Notification): NotificationResponse {
            return NotificationResponse(
                id = notification.id!!,
                title = notification.title,
                body = notification.body,
                notificationType = notification.notificationType,
                referenceId = notification.referenceId,
                isRead = notification.isRead,
                isSent = notification.isSent,
                createdAt = notification.createdAt,
                readAt = notification.readAt
            )
        }
    }
}

// 알림 읽음 처리 요청
data class MarkReadRequest(
    @field:NotNull(message = "알림 ID는 필수입니다")
    val notificationId: UUID
)

// 알림 설정 조회 응답
data class NotificationSettingsResponse(
    val reportEnabled: Boolean,
    val systemEnabled: Boolean
) {
    companion object {
        fun from(settings: NotificationSettings): NotificationSettingsResponse {
            return NotificationSettingsResponse(
                reportEnabled = settings.reportEnabled,
                systemEnabled = settings.systemEnabled
            )
        }
    }
}

// 알림 설정 업데이트 요청
data class UpdateSettingsRequest(
    val reportEnabled: Boolean,
    val systemEnabled: Boolean
)

// 읽지 않은 알림 수 응답
data class UnreadCountResponse(
    val unreadCount: Long
)