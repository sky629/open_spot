package com.kangpark.openspot.notification.repository.jpa

import com.kangpark.openspot.notification.repository.jpa.entity.DeviceTokenJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * DeviceToken JPA Repository
 * Spring Data JPA interface for DeviceTokenJpaEntity
 */
interface DeviceTokenJpaRepository : JpaRepository<DeviceTokenJpaEntity, UUID> {

    fun findByUserId(userId: UUID): List<DeviceTokenJpaEntity>

    fun findByToken(token: String): DeviceTokenJpaEntity?

    @Query("SELECT d FROM DeviceTokenJpaEntity d WHERE d.userId = :userId AND d.isActive = true")
    fun findActiveByUserId(@Param("userId") userId: UUID): List<DeviceTokenJpaEntity>

    fun existsByToken(token: String): Boolean
}
