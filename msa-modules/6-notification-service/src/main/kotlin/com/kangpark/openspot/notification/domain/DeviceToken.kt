package com.kangpark.openspot.notification.domain

import com.kangpark.openspot.common.core.domain.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "device_tokens",
    schema = "notification",
    indexes = [
        Index(name = "idx_device_tokens_user_id", columnList = "user_id"),
        Index(name = "idx_device_tokens_token", columnList = "token"),
        Index(name = "idx_device_tokens_active", columnList = "user_id, is_active"),
        Index(name = "idx_device_tokens_device_type", columnList = "device_type")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_device_tokens_token", columnNames = ["token"])
    ]
)
class DeviceToken(
    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "token", nullable = false, length = 500)
    var token: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    val deviceType: DeviceType,

    @Column(name = "device_id", length = 255)
    val deviceId: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null
) : BaseEntity() {

    fun markAsUsed() {
        lastUsedAt = LocalDateTime.now()
    }

    fun deactivate() {
        isActive = false
    }

    fun activate() {
        isActive = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceToken) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "DeviceToken(id=$id, userId=$userId, deviceType=$deviceType, isActive=$isActive)"
    }
}

enum class DeviceType {
    WEB, ANDROID, IOS
}