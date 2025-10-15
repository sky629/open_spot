package com.kangpark.openspot.notification.repository.jpa

import com.kangpark.openspot.notification.domain.vo.NotificationType
import com.kangpark.openspot.notification.repository.jpa.entity.NotificationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Notification JPA Repository
 * Spring Data JPA interface for NotificationJpaEntity
 */
interface NotificationJpaRepository : JpaRepository<NotificationJpaEntity, UUID> {

    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<NotificationJpaEntity>

    fun findByUserIdAndNotificationTypeOrderByCreatedAtDesc(
        userId: UUID,
        notificationType: NotificationType,
        pageable: Pageable
    ): Page<NotificationJpaEntity>

    @Query("SELECT n FROM NotificationJpaEntity n WHERE n.userId = :userId AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    fun findUnreadByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<NotificationJpaEntity>

    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n WHERE n.userId = :userId AND n.readAt IS NULL")
    fun countUnreadByUserId(@Param("userId") userId: UUID): Long

    fun findByReferenceId(referenceId: UUID): List<NotificationJpaEntity>

    @Query("SELECT n FROM NotificationJpaEntity n WHERE n.userId = :userId AND n.sentAt IS NULL")
    fun findUnsentByUserId(@Param("userId") userId: UUID): List<NotificationJpaEntity>
}
