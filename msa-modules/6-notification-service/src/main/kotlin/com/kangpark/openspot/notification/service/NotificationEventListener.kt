package com.kangpark.openspot.notification.service

import com.kangpark.openspot.notification.event.NotificationEvent
import com.kangpark.openspot.notification.event.ReportGeneratedEvent
import com.kangpark.openspot.notification.event.SystemNoticeEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class NotificationEventListener(
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(NotificationEventListener::class.java)

    @KafkaListener(
        topics = ["report-events"],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleReportEvent(
        @Payload event: ReportGeneratedEvent,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Received report event: reportId={}, userId={}, topic={}, partition={}, offset={}",
                event.reportId, event.userId, topic, partition, offset)

            notificationService.handleReportGeneratedEvent(event)
            acknowledgment.acknowledge()

            logger.info("Successfully processed report event: reportId={}", event.reportId)
        } catch (e: Exception) {
            logger.error("Failed to process report event: reportId={}", event.reportId, e)
            // 에러 발생 시 acknowledge하지 않아 재처리됨
            throw e
        }
    }

    @KafkaListener(
        topics = ["system-events"],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleSystemEvent(
        @Payload event: SystemNoticeEvent,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Received system notice event: title={}, topic={}, partition={}, offset={}",
                event.title, topic, partition, offset)

            notificationService.handleSystemNoticeEvent(event)
            acknowledgment.acknowledge()

            logger.info("Successfully processed system notice event: title={}", event.title)
        } catch (e: Exception) {
            logger.error("Failed to process system notice event: title={}", event.title, e)
            throw e
        }
    }

    @KafkaListener(
        topics = ["notification-events"],
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleGenericNotificationEvent(
        @Payload event: NotificationEvent,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Received generic notification event: eventId={}, type={}, topic={}, partition={}, offset={}",
                event.eventId, event::class.simpleName, topic, partition, offset)

            when (event) {
                is ReportGeneratedEvent -> notificationService.handleReportGeneratedEvent(event)
                is SystemNoticeEvent -> notificationService.handleSystemNoticeEvent(event)
                else -> {
                    logger.warn("Unknown notification event type: {}", event::class.simpleName)
                }
            }

            acknowledgment.acknowledge()
            logger.info("Successfully processed generic notification event: eventId={}", event.eventId)
        } catch (e: Exception) {
            logger.error("Failed to process generic notification event: eventId={}", event.eventId, e)
            throw e
        }
    }
}