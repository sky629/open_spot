package com.kangpark.openspot.notification.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.notification.domain.vo.NotificationType
import java.time.LocalDateTime
import java.util.*

/**
 * NotificationSettings Domain Entity
 * Pure business logic, no infrastructure dependencies
 */
data class NotificationSettings(
    val userId: UUID,
    val reportEnabled: Boolean = true,
    val systemEnabled: Boolean = true,
    // BaseEntity 합성
    val baseEntity: BaseEntity = BaseEntity()
) {
    // 편의 프로퍼티 (BaseEntity 필드 접근)
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt

    fun updateReportSetting(enabled: Boolean): NotificationSettings {
        return NotificationSettings(
            userId = userId,
            reportEnabled = enabled,
            systemEnabled = systemEnabled
        )
    }

    fun updateSystemSetting(enabled: Boolean): NotificationSettings {
        return NotificationSettings(
            userId = userId,
            reportEnabled = reportEnabled,
            systemEnabled = enabled
        )
    }

    fun updateAllSettings(reportEnabled: Boolean, systemEnabled: Boolean): NotificationSettings {
        return NotificationSettings(
            userId = userId,
            reportEnabled = reportEnabled,
            systemEnabled = systemEnabled
        )
    }

    fun isNotificationEnabled(type: NotificationType): Boolean {
        return when (type) {
            NotificationType.REPORT_COMPLETE -> reportEnabled
            NotificationType.SYSTEM_NOTICE -> systemEnabled
        }
    }

    companion object {
        fun create(userId: UUID): NotificationSettings {
            return NotificationSettings(userId = userId)
        }
    }
}