package com.kangpark.openspot.notification.repository.jpa.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.notification.domain.entity.NotificationSettings
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
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
    indexes = [Index(name = "idx_notification_settings_user_id", columnList = "user_id")],
    uniqueConstraints = [UniqueConstraint(name = "uk_notification_settings_user_id", columnNames = ["user_id"])]
)
@EntityListeners(AuditingEntityListener::class)
class NotificationSettingsJpaEntity(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,

    @Column(name = "report_enabled", nullable = false)
    val reportEnabled: Boolean = true,

    @Column(name = "system_enabled", nullable = false)
    val systemEnabled: Boolean = true,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): NotificationSettings {
        return NotificationSettings(
            userId = userId,
            reportEnabled = reportEnabled,
            systemEnabled = systemEnabled,
            baseEntity = BaseEntity(
                id = this.id,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt
            )
        )
    }

    companion object {
        fun fromDomain(settings: NotificationSettings): NotificationSettingsJpaEntity {
            return NotificationSettingsJpaEntity(
                id = settings.id,
                userId = settings.userId,
                reportEnabled = settings.reportEnabled,
                systemEnabled = settings.systemEnabled,
                createdAt = settings.createdAt,
                updatedAt = settings.updatedAt
            )
        }
    }
}
