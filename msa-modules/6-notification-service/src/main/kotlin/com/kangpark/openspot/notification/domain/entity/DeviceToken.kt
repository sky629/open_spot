package com.kangpark.openspot.notification.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.notification.domain.vo.DeviceType
import java.time.LocalDateTime
import java.util.*

/**
 * DeviceToken Domain Entity
 * Pure business logic, no infrastructure dependencies
 */
data class DeviceToken(
    val userId: UUID,
    val token: String,
    val deviceType: DeviceType,
    val deviceId: String? = null,
    val isActive: Boolean = true,
    val lastUsedAt: LocalDateTime? = null,
    // BaseEntity 합성
    val baseEntity: BaseEntity = BaseEntity()
) {
    // 편의 프로퍼티 (BaseEntity 필드 접근)
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt

    fun markAsUsed(): DeviceToken {
        return copy(
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    fun activate(): DeviceToken {
        return copy(
            isActive = true,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    fun deactivate(): DeviceToken {
        return copy(
            isActive = false,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    companion object {
        fun create(
            userId: UUID,
            token: String,
            deviceType: DeviceType,
            deviceId: String? = null
        ): DeviceToken {
            return DeviceToken(
                userId = userId,
                token = token,
                deviceType = deviceType,
                deviceId = deviceId
            )
        }
    }
}