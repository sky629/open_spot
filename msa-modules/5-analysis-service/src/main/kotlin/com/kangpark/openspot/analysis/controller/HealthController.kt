package com.kangpark.openspot.analysis.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/stores")
class HealthController {
    
    @GetMapping("/health")
    fun health(): ApiResponse<Map<String, Any>> {
        val healthData = mapOf(
            "service" to "analysis-service",
            "status" to "UP",
            "timestamp" to LocalDateTime.now()
        )
        return ApiResponse.success(healthData)
    }
}