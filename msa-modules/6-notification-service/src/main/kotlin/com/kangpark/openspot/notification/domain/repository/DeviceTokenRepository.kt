package com.kangpark.openspot.notification.domain.repository

import com.kangpark.openspot.notification.domain.entity.DeviceToken
import java.util.*

/**
 * DeviceToken Domain Repository Interface
 * Pure domain layer contract
 */
interface DeviceTokenRepository {

    fun save(deviceToken: DeviceToken): DeviceToken

    fun findById(id: UUID): DeviceToken?

    fun findByToken(token: String): DeviceToken?

    fun findByUserId(userId: UUID): List<DeviceToken>

    fun findActiveByUserId(userId: UUID): List<DeviceToken>

    fun existsByToken(token: String): Boolean

    fun deleteById(id: UUID)
}
