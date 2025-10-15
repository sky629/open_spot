package com.kangpark.openspot.notification.repository.jpa.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.notification.domain.entity.Notification
import com.kangpark.openspot.notification.domain.vo.NotificationType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "notifications",
    schema = "notification",
    indexes = [
        Index(name = "idx_notifications_user_id", columnList = "user_id"),
        Index(name = "idx_notifications_type", columnList = "notification_type"),
        Index(name = "idx_notifications_user_created", columnList = "user_id, created_at"),
        Index(name = "idx_notifications_reference", columnList = "reference_id")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class NotificationJpaEntity(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    val body: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    val notificationType: NotificationType,

    @Column(name = "reference_id")
    val referenceId: UUID? = null,

    @Column(name = "fcm_message_id")
    val fcmMessageId: String? = null,

    @Column(name = "sent_at")
    val sentAt: LocalDateTime? = null,

    @Column(name = "read_at")
    val readAt: LocalDateTime? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    fun toDomain(): Notification {
        return Notification(
            userId = userId,
            title = title,
            body = body,
            notificationType = notificationType,
            referenceId = referenceId,
            fcmMessageId = fcmMessageId,
            sentAt = sentAt,
            readAt = readAt,
            baseEntity = BaseEntity(
                id = this.id,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt
            )
        )
    }

    companion object {
        fun fromDomain(notification: Notification): NotificationJpaEntity {
            return NotificationJpaEntity(
                id = notification.id,
                userId = notification.userId,
                title = notification.title,
                body = notification.body,
                notificationType = notification.notificationType,
                referenceId = notification.referenceId,
                fcmMessageId = notification.fcmMessageId,
                sentAt = notification.sentAt,
                readAt = notification.readAt,
                createdAt = notification.createdAt,
                updatedAt = notification.updatedAt
            )
        }
    }
}
