package com.kangpark.openspot.notification.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.IOException

@Configuration
@ConfigurationProperties(prefix = "fcm")
class FCMConfig {

    private val logger = LoggerFactory.getLogger(FCMConfig::class.java)

    lateinit var serviceAccountKeyPath: String
    lateinit var projectId: String
    var enabled: Boolean = true

    @Bean
    fun firebaseApp(): FirebaseApp? {
        if (!enabled) {
            logger.warn("FCM is disabled. Firebase app will not be initialized.")
            return null
        }

        return try {
            // 설정값 검증
            if (!::serviceAccountKeyPath.isInitialized || !::projectId.isInitialized) {
                throw IllegalStateException("FCM configuration is incomplete. Check serviceAccountKeyPath and projectId.")
            }

            val resource: Resource = ClassPathResource(serviceAccountKeyPath)
            if (!resource.exists()) {
                throw IOException("Firebase service account key file not found: $serviceAccountKeyPath")
            }

            val serviceAccount = resource.inputStream

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId(projectId)
                .build()

            val app = if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            } else {
                FirebaseApp.getInstance()
            }

            logger.info("Firebase app initialized successfully for project: {}", projectId)
            app

        } catch (e: Exception) {
            logger.error("Failed to initialize Firebase app. FCM notifications will be disabled. Error: {}", e.message, e)
            null
        }
    }

    @Bean
    fun firebaseMessaging(firebaseApp: FirebaseApp?): FirebaseMessaging? {
        return firebaseApp?.let {
            FirebaseMessaging.getInstance(it)
        } ?: run {
            logger.warn("FirebaseApp is null. FirebaseMessaging will not be available.")
            null
        }
    }
}