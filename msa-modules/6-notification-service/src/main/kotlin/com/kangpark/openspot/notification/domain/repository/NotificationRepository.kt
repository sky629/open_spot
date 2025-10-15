package com.kangpark.openspot.notification.domain.repository

import com.kangpark.openspot.notification.domain.entity.Notification
import com.kangpark.openspot.notification.domain.vo.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Notification Domain Repository Interface
 * Pure domain layer contract
 */
interface NotificationRepository {

    fun save(notification: Notification): Notification

    fun findById(id: UUID): Notification?

    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Notification>

    fun findByUserIdAndNotificationTypeOrderByCreatedAtDesc(
        userId: UUID,
        notificationType: NotificationType,
        pageable: Pageable
    ): Page<Notification>

    fun findUnreadByUserId(userId: UUID, pageable: Pageable): Page<Notification>

    fun countUnreadByUserId(userId: UUID): Long

    fun findByReferenceId(referenceId: UUID): List<Notification>

    fun findUnsentByUserId(userId: UUID): List<Notification>

    fun deleteById(id: UUID)
}
