package com.kangpark.openspot.notification.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/notifications")
class HealthController {
    
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        return mapOf(
            "service" to "notification-service",
            "status" to "UP",
            "timestamp" to LocalDateTime.now()
        )
    }
}