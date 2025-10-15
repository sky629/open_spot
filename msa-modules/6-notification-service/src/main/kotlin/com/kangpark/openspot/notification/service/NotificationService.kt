package com.kangpark.openspot.notification.service

import com.kangpark.openspot.notification.domain.entity.DeviceToken
import com.kangpark.openspot.notification.domain.entity.Notification
import com.kangpark.openspot.notification.domain.entity.NotificationSettings
import com.kangpark.openspot.notification.domain.vo.DeviceType
import com.kangpark.openspot.notification.domain.vo.NotificationType
import com.kangpark.openspot.notification.event.ReportGeneratedEvent
import com.kangpark.openspot.notification.event.SystemNoticeEvent
import com.kangpark.openspot.notification.domain.repository.DeviceTokenRepository
import com.kangpark.openspot.notification.domain.repository.NotificationRepository
import com.kangpark.openspot.notification.domain.repository.NotificationSettingsRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val deviceTokenRepository: DeviceTokenRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val fcmService: FCMService
) {

    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    /**
     * 분석 리포트 생성 완료 이벤트 처리
     */
    fun handleReportGeneratedEvent(event: ReportGeneratedEvent) {
        logger.info("Handling report generated event: reportId={}, userId={}", event.reportId, event.userId)

        // 사용자 알림 설정 확인
        val settings = getOrCreateNotificationSettings(event.userId)
        if (!settings.isNotificationEnabled(NotificationType.REPORT_COMPLETE)) {
            logger.info("Report notification disabled for user: {}", event.userId)
            return
        }

        // 알림 생성
        val notification = Notification(
            userId = event.userId,
            title = "분석 리포트가 완성되었습니다",
            body = "${event.location}의 상권 분석 리포트 '${event.reportTitle}'가 완성되었습니다. 지금 확인해보세요!",
            notificationType = NotificationType.REPORT_COMPLETE,
            referenceId = event.reportId
        )

        val savedNotification = notificationRepository.save(notification)
        logger.info("Created notification: id={}, userId={}", savedNotification.id, event.userId)

        // FCM 푸시 전송
        sendPushNotification(savedNotification)
    }

    /**
     * 시스템 공지 이벤트 처리
     */
    fun handleSystemNoticeEvent(event: SystemNoticeEvent) {
        logger.info("Handling system notice event: title={}, targetUsers={}",
            event.title, event.targetUserIds?.size ?: "all")

        val targetUserIds = event.targetUserIds ?: getAllActiveUserIds()

        targetUserIds.forEach { userId ->
            // 사용자 알림 설정 확인
            val settings = getOrCreateNotificationSettings(userId)
            if (!settings.isNotificationEnabled(NotificationType.SYSTEM_NOTICE)) {
                logger.debug("System notification disabled for user: {}", userId)
                return@forEach
            }

            // 알림 생성
            val notification = Notification(
                userId = userId,
                title = event.title,
                body = event.message,
                notificationType = NotificationType.SYSTEM_NOTICE
            )

            val savedNotification = notificationRepository.save(notification)
            logger.debug("Created system notification: id={}, userId={}", savedNotification.id, userId)

            // FCM 푸시 전송
            sendPushNotification(savedNotification)
        }
    }

    /**
     * FCM 푸시 알림 전송
     */
    private fun sendPushNotification(notification: Notification) {
        try {
            val deviceTokens = deviceTokenRepository.findActiveByUserId(notification.userId)

            if (deviceTokens.isEmpty()) {
                logger.warn("No active device tokens found for user: {}", notification.userId)
                return
            }

            logger.info("Sending push notification to {} devices for user: {}",
                deviceTokens.size, notification.userId)

            if (deviceTokens.size == 1) {
                // 단일 디바이스 전송
                val messageId = fcmService.sendNotification(deviceTokens.first(), notification)
                if (messageId != null) {
                    val updated = notification.markAsSent(messageId)
                    notificationRepository.save(updated)
                }
            } else {
                // 다중 디바이스 전송
                val response = fcmService.sendMulticastNotification(deviceTokens, notification)
                response?.let {
                    if (it.successCount > 0) {
                        val updated = notification.markAsSent("multicast_${UUID.randomUUID()}")
                        notificationRepository.save(updated)
                    }

                    // 실패한 토큰들 비활성화
                    it.responses.forEachIndexed { index, sendResponse ->
                        if (!sendResponse.isSuccessful) {
                            val token = deviceTokens[index]
                            logger.warn("Deactivating failed token: {}", token.token.take(10) + "...")
                            val deactivated = token.deactivate()
                            deviceTokenRepository.save(deactivated)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to send push notification: notificationId={}", notification.id, e)
        }
    }

    /**
     * 디바이스 토큰 등록/업데이트
     */
    fun registerDeviceToken(userId: UUID, token: String, deviceType: DeviceType, deviceId: String?): DeviceToken {
        // 기존 토큰 확인
        val existingToken = deviceTokenRepository.findByToken(token)

        return if (existingToken != null) {
            // 기존 토큰 업데이트
            var updated = existingToken.markAsUsed()
            if (!updated.isActive) {
                updated = updated.activate()
            }
            deviceTokenRepository.save(updated)
        } else {
            // 새 토큰 생성
            val newToken = DeviceToken(
                userId = userId,
                token = token,
                deviceType = deviceType,
                deviceId = deviceId
            )
            deviceTokenRepository.save(newToken)
        }
    }

    /**
     * 사용자 알림 목록 조회
     */
    @Transactional(readOnly = true)
    fun getUserNotifications(userId: UUID, pageable: Pageable): Page<Notification> {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    @Transactional(readOnly = true)
    fun getUnreadNotificationCount(userId: UUID): Long {
        return notificationRepository.countUnreadByUserId(userId)
    }

    /**
     * 알림 읽음 처리
     */
    fun markNotificationAsRead(userId: UUID, notificationId: UUID): Boolean {
        val notification = notificationRepository.findById(notificationId)

        return if (notification != null && notification.userId == userId) {
            val updated = notification.markAsRead()
            notificationRepository.save(updated)
            true
        } else {
            false
        }
    }

    /**
     * 알림 설정 조회/생성
     */
    private fun getOrCreateNotificationSettings(userId: UUID): NotificationSettings {
        return notificationSettingsRepository.findByUserId(userId)
            ?: notificationSettingsRepository.save(NotificationSettings(userId = userId))
    }

    /**
     * 알림 설정 조회
     */
    @Transactional(readOnly = true)
    fun getNotificationSettings(userId: UUID): NotificationSettings {
        return getOrCreateNotificationSettings(userId)
    }

    /**
     * 알림 설정 업데이트
     */
    fun updateNotificationSettings(userId: UUID, reportEnabled: Boolean, systemEnabled: Boolean): NotificationSettings {
        val settings = getOrCreateNotificationSettings(userId)
        val updated = settings.updateAllSettings(reportEnabled, systemEnabled)
        return notificationSettingsRepository.save(updated)
    }

    /**
     * 활성 사용자 ID 목록 조회 (실제 구현에서는 auth-service 연동 필요)
     */
    private fun getAllActiveUserIds(): List<UUID> {
        // TODO: auth-service와 연동하여 활성 사용자 목록 조회
        // 현재는 알림 설정이 있는 사용자들 반환
        return notificationSettingsRepository.findAll().map { it.userId }
    }
}