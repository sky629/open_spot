package com.kangpark.openspot.notification.repository

import com.kangpark.openspot.notification.domain.Notification
import com.kangpark.openspot.notification.domain.NotificationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {

    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Notification>

    fun findByUserIdAndNotificationTypeOrderByCreatedAtDesc(
        userId: UUID,
        notificationType: NotificationType,
        pageable: Pageable
    ): Page<Notification>

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    fun findUnreadByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<Notification>

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.readAt IS NULL")
    fun countUnreadByUserId(@Param("userId") userId: UUID): Long

    fun findByReferenceId(referenceId: UUID): List<Notification>

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.sentAt IS NULL")
    fun findUnsentByUserId(@Param("userId") userId: UUID): List<Notification>
}