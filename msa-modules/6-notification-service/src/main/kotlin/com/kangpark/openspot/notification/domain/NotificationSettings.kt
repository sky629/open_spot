package com.kangpark.openspot.notification.domain

import com.kangpark.openspot.notification.domain.vo.NotificationType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
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
@EntityListeners(AuditingEntityListener::class)
class NotificationSettings(
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,

    @Column(name = "report_enabled", nullable = false)
    var reportEnabled: Boolean = true,

    @Column(name = "system_enabled", nullable = false)
    var systemEnabled: Boolean = true,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

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