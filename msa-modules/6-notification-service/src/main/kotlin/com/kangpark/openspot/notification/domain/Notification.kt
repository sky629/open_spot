package com.kangpark.openspot.notification.domain

import jakarta.persistence.*
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
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

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
    var fcmMessageId: String? = null,

    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,

    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    fun markAsSent(messageId: String) {
        fcmMessageId = messageId
        sentAt = LocalDateTime.now()
    }

    fun markAsRead() {
        if (readAt == null) {
            readAt = LocalDateTime.now()
        }
    }

    val isRead: Boolean
        get() = readAt != null

    val isSent: Boolean
        get() = sentAt != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Notification) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Notification(id=$id, userId=$userId, type=$notificationType, title=$title)"
    }
}

enum class NotificationType {
    REPORT_COMPLETE, SYSTEM_NOTICE
}