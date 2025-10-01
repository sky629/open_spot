package com.kangpark.openspot.location.service.event

import com.kangpark.openspot.location.domain.entity.Location
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * Location Event Publisher (개인 기록 서비스)
 */
@Component
class LocationEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(LocationEventPublisher::class.java)

    companion object {
        const val LOCATION_EVENTS_TOPIC = "location-events"
    }

    /**
     * 장소 생성 이벤트 발행
     */
    fun publishLocationCreated(location: Location) {
        val event = LocationCreatedEvent(
            locationId = location.id,
            userId = location.userId,
            name = location.name,
            categoryId = location.categoryId,
            coordinates = CoordinatesDto(
                latitude = location.coordinates.latitude.toDouble(),
                longitude = location.coordinates.longitude.toDouble()
            ),
            timestamp = LocalDateTime.now()
        )

        publishEvent(LOCATION_EVENTS_TOPIC, event.locationId.toString(), event)
        logger.info("Published LocationCreatedEvent: locationId={}", event.locationId)
    }

    /**
     * 장소 업데이트 이벤트 발행
     */
    fun publishLocationUpdated(location: Location) {
        val event = LocationUpdatedEvent(
            locationId = location.id,
            userId = location.userId,
            name = location.name,
            categoryId = location.categoryId,
            timestamp = LocalDateTime.now()
        )

        publishEvent(LOCATION_EVENTS_TOPIC, event.locationId.toString(), event)
        logger.info("Published LocationUpdatedEvent: locationId={}", event.locationId)
    }

    /**
     * 장소 비활성화 이벤트 발행
     */
    fun publishLocationDeactivated(location: Location) {
        val event = LocationDeactivatedEvent(
            locationId = location.id,
            userId = location.userId,
            name = location.name,
            timestamp = LocalDateTime.now()
        )

        publishEvent(LOCATION_EVENTS_TOPIC, event.locationId.toString(), event)
        logger.info("Published LocationDeactivatedEvent: locationId={}", event.locationId)
    }

    private fun publishEvent(topic: String, key: String, event: Any) {
        try {
            kafkaTemplate.send(topic, key, event)
        } catch (e: Exception) {
            logger.error("Failed to publish event to topic: {}, key: {}, event: {}", topic, key, event, e)
        }
    }

    // Event DTOs
    data class CoordinatesDto(
        val latitude: Double,
        val longitude: Double
    )

    data class LocationCreatedEvent(
        val locationId: UUID,
        val userId: UUID,
        val name: String,
        val categoryId: UUID,
        val coordinates: CoordinatesDto,
        val timestamp: LocalDateTime
    )

    data class LocationUpdatedEvent(
        val locationId: UUID,
        val userId: UUID,
        val name: String,
        val categoryId: UUID,
        val timestamp: LocalDateTime
    )

    data class LocationDeactivatedEvent(
        val locationId: UUID,
        val userId: UUID,
        val name: String,
        val timestamp: LocalDateTime
    )
}