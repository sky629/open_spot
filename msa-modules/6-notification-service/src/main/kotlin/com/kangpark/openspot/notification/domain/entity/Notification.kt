package com.kangpark.openspot.notification.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.notification.domain.vo.NotificationType
import java.time.LocalDateTime
import java.util.*

/**
 * Notification Domain Entity
 * Pure business logic, no infrastructure dependencies
 */
data class Notification(
    val userId: UUID,
    val title: String,
    val body: String,
    val notificationType: NotificationType,
    val referenceId: UUID? = null,
    val fcmMessageId: String? = null,
    val sentAt: LocalDateTime? = null,
    val readAt: LocalDateTime? = null,
    // BaseEntity 합성
    val baseEntity: BaseEntity = BaseEntity()
) {
    // 편의 프로퍼티 (BaseEntity 필드 접근)
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt

    fun markAsSent(messageId: String): Notification {
        return Notification(
            userId = userId,
            title = title,
            body = body,
            notificationType = notificationType,
            referenceId = referenceId,
            fcmMessageId = messageId,
            sentAt = LocalDateTime.now(),
            readAt = readAt
        )
    }

    fun markAsRead(): Notification {
        if (readAt != null) {
            return this
        }
        return Notification(
            userId = userId,
            title = title,
            body = body,
            notificationType = notificationType,
            referenceId = referenceId,
            fcmMessageId = fcmMessageId,
            sentAt = sentAt,
            readAt = LocalDateTime.now()
        )
    }

    val isRead: Boolean
        get() = readAt != null

    val isSent: Boolean
        get() = sentAt != null

    companion object {
        fun create(
            userId: UUID,
            title: String,
            body: String,
            notificationType: NotificationType,
            referenceId: UUID? = null
        ): Notification {
            return Notification(
                userId = userId,
                title = title,
                body = body,
                notificationType = notificationType,
                referenceId = referenceId
            )
        }
    }
}