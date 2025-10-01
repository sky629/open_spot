package com.kangpark.openspot.notification.repository

import com.kangpark.openspot.notification.domain.DeviceToken
import com.kangpark.openspot.notification.domain.vo.DeviceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceToken, UUID> {

    fun findByUserIdAndIsActiveTrue(userId: UUID): List<DeviceToken>

    fun findByUserIdAndDeviceTypeAndIsActiveTrue(userId: UUID, deviceType: DeviceType): List<DeviceToken>

    fun findByToken(token: String): DeviceToken?

    fun existsByToken(token: String): Boolean

    @Query("SELECT dt FROM DeviceToken dt WHERE dt.userId IN :userIds AND dt.isActive = true")
    fun findActiveTokensByUserIds(@Param("userIds") userIds: List<UUID>): List<DeviceToken>

    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE dt.token = :token")
    fun deactivateByToken(@Param("token") token: String): Int

    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.isActive = false WHERE dt.userId = :userId")
    fun deactivateAllByUserId(@Param("userId") userId: UUID): Int

    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.lastUsedAt = CURRENT_TIMESTAMP WHERE dt.token = :token")
    fun updateLastUsedAt(@Param("token") token: String): Int
}