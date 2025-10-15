package com.kangpark.openspot.notification.repository.jpa.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.notification.domain.entity.DeviceToken
import com.kangpark.openspot.notification.domain.vo.DeviceType
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
    name = "device_tokens",
    schema = "notification",
    indexes = [
        Index(name = "idx_device_tokens_user_id", columnList = "user_id"),
        Index(name = "idx_device_tokens_token", columnList = "token"),
        Index(name = "idx_device_tokens_active", columnList = "user_id, is_active")
    ],
    uniqueConstraints = [UniqueConstraint(name = "uk_device_tokens_token", columnNames = ["token"])]
)
@EntityListeners(AuditingEntityListener::class)
class DeviceTokenJpaEntity(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "token", nullable = false, length = 500)
    val token: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    val deviceType: DeviceType,

    @Column(name = "device_id", length = 255)
    val deviceId: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "last_used_at")
    val lastUsedAt: LocalDateTime? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): DeviceToken {
        return DeviceToken(
            userId = userId,
            token = token,
            deviceType = deviceType,
            deviceId = deviceId,
            isActive = isActive,
            lastUsedAt = lastUsedAt,
            baseEntity = BaseEntity(
                id = this.id,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt
            )
        )
    }

    companion object {
        fun fromDomain(token: DeviceToken): DeviceTokenJpaEntity {
            return DeviceTokenJpaEntity(
                id = token.id,
                userId = token.userId,
                token = token.token,
                deviceType = token.deviceType,
                deviceId = token.deviceId,
                isActive = token.isActive,
                lastUsedAt = token.lastUsedAt,
                createdAt = token.createdAt,
                updatedAt = token.updatedAt
            )
        }
    }
}
