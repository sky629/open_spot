package com.kangpark.openspot.notification.domain.repository

import com.kangpark.openspot.notification.domain.entity.NotificationSettings
import java.util.*

/**
 * NotificationSettings Domain Repository Interface
 * Pure domain layer contract
 */
interface NotificationSettingsRepository {

    fun save(settings: NotificationSettings): NotificationSettings

    fun findById(id: UUID): NotificationSettings?

    fun findByUserId(userId: UUID): NotificationSettings?

    fun existsByUserId(userId: UUID): Boolean

    fun findAll(): List<NotificationSettings>

    fun deleteById(id: UUID)
}
