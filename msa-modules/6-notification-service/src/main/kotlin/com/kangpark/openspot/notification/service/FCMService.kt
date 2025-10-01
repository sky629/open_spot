package com.kangpark.openspot.notification.service

import com.google.firebase.messaging.*
import com.kangpark.openspot.notification.domain.DeviceToken
import com.kangpark.openspot.notification.domain.vo.DeviceType
import com.kangpark.openspot.notification.domain.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class FCMService(
    private val firebaseMessaging: FirebaseMessaging?
) {

    private val logger = LoggerFactory.getLogger(FCMService::class.java)

    /**
     * 단일 디바이스로 알림 전송
     */
    fun sendNotification(deviceToken: DeviceToken, notification: Notification): String? {
        if (firebaseMessaging == null) {
            logger.warn("FirebaseMessaging is not available. Notification will not be sent.")
            return null
        }

        return try {
            val message = buildMessage(deviceToken, notification)
            val response = firebaseMessaging.send(message)

            logger.info("Successfully sent notification to device: token={}, messageId={}",
                deviceToken.token.take(10) + "...", response)

            response
        } catch (e: Exception) {
            logger.error("Failed to send notification to device: token={}, error={}",
                deviceToken.token.take(10) + "...", e.message, e)
            null
        }
    }

    /**
     * 다중 디바이스로 동일한 알림 전송
     */
    fun sendMulticastNotification(deviceTokens: List<DeviceToken>, notification: Notification): BatchResponse? {
        if (firebaseMessaging == null) {
            logger.warn("FirebaseMessaging is not available. Multicast notification will not be sent.")
            return null
        }

        if (deviceTokens.isEmpty()) {
            logger.warn("No device tokens provided for multicast notification")
            return null
        }

        return try {
            val tokens = deviceTokens.map { it.token }
            val multicastMessage = buildMulticastMessage(deviceTokens, notification)

            val response = firebaseMessaging.sendEachForMulticast(multicastMessage)

            logger.info("Multicast notification sent: successCount={}, failureCount={}, tokens={}",
                response.successCount, response.failureCount, tokens.size)

            // 실패한 토큰들 로깅
            response.responses.forEachIndexed { index, sendResponse ->
                if (!sendResponse.isSuccessful) {
                    logger.warn("Failed to send to token: {}, error: {}",
                        tokens[index].take(10) + "...", sendResponse.exception?.message)
                }
            }

            response
        } catch (e: Exception) {
            logger.error("Failed to send multicast notification: tokenCount={}, error={}",
                deviceTokens.size, e.message, e)
            null
        }
    }

    /**
     * 토픽 기반 알림 전송
     */
    fun sendTopicNotification(topic: String, notification: Notification): String? {
        if (firebaseMessaging == null) {
            logger.warn("FirebaseMessaging is not available. Topic notification will not be sent.")
            return null
        }

        return try {
            val message = Message.builder()
                .setTopic(topic)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle(notification.title)
                        .setBody(notification.body)
                        .build()
                )
                .putAllData(buildDataPayload(notification))
                .build()

            val response = firebaseMessaging.send(message)

            logger.info("Successfully sent topic notification: topic={}, messageId={}", topic, response)
            response
        } catch (e: Exception) {
            logger.error("Failed to send topic notification: topic={}, error={}", topic, e.message, e)
            null
        }
    }

    private fun buildMessage(deviceToken: DeviceToken, notification: Notification): Message {
        val messageBuilder = Message.builder()
            .setToken(deviceToken.token)
            .setNotification(
                com.google.firebase.messaging.Notification.builder()
                    .setTitle(notification.title)
                    .setBody(notification.body)
                    .build()
            )
            .putAllData(buildDataPayload(notification))

        // 디바이스 타입별 설정
        when (deviceToken.deviceType) {
            DeviceType.ANDROID -> {
                messageBuilder.setAndroidConfig(
                    AndroidConfig.builder()
                        .setNotification(
                            AndroidNotification.builder()
                                .setIcon("ic_notification")
                                .setColor("#1976D2")
                                .setSound("default")
                                .setPriority(AndroidNotification.Priority.HIGH)
                                .build()
                        )
                        .build()
                )
            }
            DeviceType.IOS -> {
                messageBuilder.setApnsConfig(
                    ApnsConfig.builder()
                        .setAps(
                            Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build()
                        )
                        .build()
                )
            }
            DeviceType.WEB -> {
                messageBuilder.setWebpushConfig(
                    WebpushConfig.builder()
                        .setNotification(
                            WebpushNotification.builder()
                                .setIcon("/icon-192x192.png")
                                .setBadge("/badge-72x72.png")
                                .setRequireInteraction(true)
                                .build()
                        )
                        .build()
                )
            }
        }

        return messageBuilder.build()
    }

    private fun buildMulticastMessage(deviceTokens: List<DeviceToken>, notification: Notification): MulticastMessage {
        val tokens = deviceTokens.map { it.token }

        return MulticastMessage.builder()
            .addAllTokens(tokens)
            .setNotification(
                com.google.firebase.messaging.Notification.builder()
                    .setTitle(notification.title)
                    .setBody(notification.body)
                    .build()
            )
            .putAllData(buildDataPayload(notification))
            .build()
    }

    private fun buildDataPayload(notification: Notification): Map<String, String> {
        val data = mutableMapOf<String, String>()

        data["notificationId"] = notification.id.toString()
        data["notificationType"] = notification.notificationType.name
        data["userId"] = notification.userId.toString()
        data["createdAt"] = notification.createdAt.toString()

        notification.referenceId?.let {
            data["referenceId"] = it.toString()
        }

        return data
    }
}