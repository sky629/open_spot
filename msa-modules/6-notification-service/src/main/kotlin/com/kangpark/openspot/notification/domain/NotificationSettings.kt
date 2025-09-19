package com.kangpark.openspot.notification.domain

import com.kangpark.openspot.common.core.domain.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "notification_settings",
    schema = "notification",
    indexes = [
        Index(name = "idx_notification_settings_user_id", columnList = "user_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_notification_settings_user_id", columnNames = ["user_id"])
    ]
)
class NotificationSettings(
    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,

    @Column(name = "report_enabled", nullable = false)
    var reportEnabled: Boolean = true,

    @Column(name = "system_enabled", nullable = false)
    var systemEnabled: Boolean = true
) : BaseEntity() {

    fun updateReportSetting(enabled: Boolean) {
        reportEnabled = enabled
    }

    fun updateSystemSetting(enabled: Boolean) {
        systemEnabled = enabled
    }

    fun updateAllSettings(reportEnabled: Boolean, systemEnabled: Boolean) {
        this.reportEnabled = reportEnabled
        this.systemEnabled = systemEnabled
    }

    fun isNotificationEnabled(type: NotificationType): Boolean {
        return when (type) {
            NotificationType.REPORT_COMPLETE -> reportEnabled
            NotificationType.SYSTEM_NOTICE -> systemEnabled
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationSettings) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "NotificationSettings(id=$id, userId=$userId, reportEnabled=$reportEnabled, systemEnabled=$systemEnabled)"
    }
}