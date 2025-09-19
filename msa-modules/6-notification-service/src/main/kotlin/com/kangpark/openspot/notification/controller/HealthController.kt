package com.kangpark.openspot.notification.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@Tag(name = "Health", description = "알림 서비스 헬스체크 API")
@RestController
@RequestMapping("/api/v1/notifications")
class HealthController {

    @Operation(summary = "헬스체크", description = "알림 서비스의 상태를 확인합니다.")
    @GetMapping("/health")
    fun health(): ApiResponse<Map<String, Any>> {
        val healthData = mapOf(
            "service" to "notification-service",
            "status" to "UP",
            "timestamp" to LocalDateTime.now(),
            "features" to listOf("FCM Push Notifications", "Kafka Event Processing", "Database Storage")
        )

        return ApiResponse.success(healthData)
    }
}