package com.kangpark.openspot.notification.repository.impl

import com.kangpark.openspot.notification.domain.entity.NotificationSettings
import com.kangpark.openspot.notification.domain.repository.NotificationSettingsRepository
import com.kangpark.openspot.notification.repository.jpa.NotificationSettingsJpaRepository
import com.kangpark.openspot.notification.repository.jpa.entity.NotificationSettingsJpaEntity
import org.springframework.stereotype.Repository
import java.util.*

/**
 * NotificationSettings Repository Implementation
 * Implements Domain Repository by bridging to JPA Repository
 */
@Repository
class NotificationSettingsRepositoryImpl(
    private val jpaRepository: NotificationSettingsJpaRepository
) : NotificationSettingsRepository {

    override fun save(settings: NotificationSettings): NotificationSettings {
        val jpaEntity = NotificationSettingsJpaEntity.fromDomain(settings)
        val saved = jpaRepository.save(jpaEntity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): NotificationSettings? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserId(userId: UUID): NotificationSettings? {
        return jpaRepository.findByUserId(userId)?.toDomain()
    }

    override fun existsByUserId(userId: UUID): Boolean {
        return jpaRepository.existsByUserId(userId)
    }

    override fun findAll(): List<NotificationSettings> {
        return jpaRepository.findAll().map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
