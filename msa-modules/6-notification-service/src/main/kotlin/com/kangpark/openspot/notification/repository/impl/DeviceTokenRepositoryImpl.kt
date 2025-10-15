package com.kangpark.openspot.notification.repository.impl

import com.kangpark.openspot.notification.domain.entity.DeviceToken
import com.kangpark.openspot.notification.domain.repository.DeviceTokenRepository
import com.kangpark.openspot.notification.repository.jpa.DeviceTokenJpaRepository
import com.kangpark.openspot.notification.repository.jpa.entity.DeviceTokenJpaEntity
import org.springframework.stereotype.Repository
import java.util.*

/**
 * DeviceToken Repository Implementation
 * Implements Domain Repository by bridging to JPA Repository
 */
@Repository
class DeviceTokenRepositoryImpl(
    private val jpaRepository: DeviceTokenJpaRepository
) : DeviceTokenRepository {

    override fun save(deviceToken: DeviceToken): DeviceToken {
        val jpaEntity = DeviceTokenJpaEntity.fromDomain(deviceToken)
        val saved = jpaRepository.save(jpaEntity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): DeviceToken? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByToken(token: String): DeviceToken? {
        return jpaRepository.findByToken(token)?.toDomain()
    }

    override fun findByUserId(userId: UUID): List<DeviceToken> {
        return jpaRepository.findByUserId(userId)
            .map { it.toDomain() }
    }

    override fun findActiveByUserId(userId: UUID): List<DeviceToken> {
        return jpaRepository.findActiveByUserId(userId)
            .map { it.toDomain() }
    }

    override fun existsByToken(token: String): Boolean {
        return jpaRepository.existsByToken(token)
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
