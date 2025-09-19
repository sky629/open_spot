package com.kangpark.openspot.notification.event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDateTime
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ReportGeneratedEvent::class, name = "REPORT_GENERATED"),
    JsonSubTypes.Type(value = SystemNoticeEvent::class, name = "SYSTEM_NOTICE")
)
abstract class NotificationEvent(
    val eventId: UUID = UUID.randomUUID(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class ReportGeneratedEvent(
    val userId: UUID,
    val reportId: UUID,
    val reportTitle: String,
    val location: String
) : NotificationEvent()

data class SystemNoticeEvent(
    val targetUserIds: List<UUID>? = null,  // null이면 전체 사용자
    val title: String,
    val message: String,
    val priority: NoticePriority = NoticePriority.NORMAL
) : NotificationEvent()

enum class NoticePriority {
    LOW, NORMAL, HIGH, URGENT
}