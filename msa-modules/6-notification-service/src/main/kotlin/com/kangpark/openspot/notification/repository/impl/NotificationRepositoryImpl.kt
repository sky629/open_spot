package com.kangpark.openspot.notification.repository.impl

import com.kangpark.openspot.notification.domain.entity.Notification
import com.kangpark.openspot.notification.domain.repository.NotificationRepository
import com.kangpark.openspot.notification.domain.vo.NotificationType
import com.kangpark.openspot.notification.repository.jpa.NotificationJpaRepository
import com.kangpark.openspot.notification.repository.jpa.entity.NotificationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Notification Repository Implementation
 * Implements Domain Repository by bridging to JPA Repository
 */
@Repository
class NotificationRepositoryImpl(
    private val jpaRepository: NotificationJpaRepository
) : NotificationRepository {

    override fun save(notification: Notification): Notification {
        val jpaEntity = NotificationJpaEntity.fromDomain(notification)
        val saved = jpaRepository.save(jpaEntity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): Notification? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Notification> {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { it.toDomain() }
    }

    override fun findByUserIdAndNotificationTypeOrderByCreatedAtDesc(
        userId: UUID,
        notificationType: NotificationType,
        pageable: Pageable
    ): Page<Notification> {
        return jpaRepository.findByUserIdAndNotificationTypeOrderByCreatedAtDesc(userId, notificationType, pageable)
            .map { it.toDomain() }
    }

    override fun findUnreadByUserId(userId: UUID, pageable: Pageable): Page<Notification> {
        return jpaRepository.findUnreadByUserId(userId, pageable)
            .map { it.toDomain() }
    }

    override fun countUnreadByUserId(userId: UUID): Long {
        return jpaRepository.countUnreadByUserId(userId)
    }

    override fun findByReferenceId(referenceId: UUID): List<Notification> {
        return jpaRepository.findByReferenceId(referenceId)
            .map { it.toDomain() }
    }

    override fun findUnsentByUserId(userId: UUID): List<Notification> {
        return jpaRepository.findUnsentByUserId(userId)
            .map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
