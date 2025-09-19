package com.kangpark.openspot.notification.repository

import com.kangpark.openspot.notification.domain.NotificationSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationSettingsRepository : JpaRepository<NotificationSettings, UUID> {

    fun findByUserId(userId: UUID): NotificationSettings?

    fun existsByUserId(userId: UUID): Boolean
}