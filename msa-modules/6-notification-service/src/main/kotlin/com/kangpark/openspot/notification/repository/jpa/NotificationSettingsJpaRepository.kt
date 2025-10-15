package com.kangpark.openspot.notification.repository.jpa

import com.kangpark.openspot.notification.repository.jpa.entity.NotificationSettingsJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * NotificationSettings JPA Repository
 * Spring Data JPA interface for NotificationSettingsJpaEntity
 */
interface NotificationSettingsJpaRepository : JpaRepository<NotificationSettingsJpaEntity, UUID> {

    fun findByUserId(userId: UUID): NotificationSettingsJpaEntity?

    fun existsByUserId(userId: UUID): Boolean
}
