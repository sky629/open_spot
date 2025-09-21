package com.kangpark.openspot.location.service.event

import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.entity.Review
import com.kangpark.openspot.location.domain.entity.LocationVisit
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class LocationEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(LocationEventPublisher::class.java)

    companion object {
        const val LOCATION_EVENTS_TOPIC = "location-events"
        const val REVIEW_EVENTS_TOPIC = "review-events"
        const val VISIT_EVENTS_TOPIC = "visit-events"
    }

    /**
     * 장소 생성 이벤트 발행
     */
    fun publishLocationCreated(location: Location) {
        val event = LocationCreatedEvent(
            locationId = location.id!!,
            name = location.name,
            category = location.category.name,
            createdBy = location.createdBy,
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
            locationId = location.id!!,
            name = location.name,
            category = location.category.name,
            timestamp = LocalDateTime.now()
        )

        publishEvent(LOCATION_EVENTS_TOPIC, event.locationId.toString(), event)
        logger.info("Published LocationUpdatedEvent: locationId={}", event.locationId)
    }

    /**
     * 장소 조회 이벤트 발행
     */
    fun publishLocationViewed(location: Location, userId: UUID?) {
        val event = LocationViewedEvent(
            locationId = location.id!!,
            userId = userId,
            viewCount = location.viewCount,
            timestamp = LocalDateTime.now()
        )

        publishEvent(LOCATION_EVENTS_TOPIC, event.locationId.toString(), event)
        logger.debug("Published LocationViewedEvent: locationId={}, userId={}", event.locationId, userId)
    }

    /**
     * 장소 비활성화 이벤트 발행
     */
    fun publishLocationDeactivated(location: Location) {
        val event = LocationDeactivatedEvent(
            locationId = location.id!!,
            name = location.name,
            createdBy = location.createdBy,
            timestamp = LocalDateTime.now()
        )

        publishEvent(LOCATION_EVENTS_TOPIC, event.locationId.toString(), event)
        logger.info("Published LocationDeactivatedEvent: locationId={}", event.locationId)
    }

    /**
     * 리뷰 생성 이벤트 발행
     */
    fun publishReviewCreated(review: Review) {
        val event = ReviewCreatedEvent(
            reviewId = review.id!!,
            locationId = review.locationId,
            userId = review.userId,
            rating = review.rating.score.toDouble(),
            hasImages = review.imageUrls.isNotEmpty(),
            timestamp = LocalDateTime.now()
        )

        publishEvent(REVIEW_EVENTS_TOPIC, event.reviewId.toString(), event)
        logger.info("Published ReviewCreatedEvent: reviewId={}, locationId={}", event.reviewId, event.locationId)
    }

    /**
     * 리뷰 업데이트 이벤트 발행
     */
    fun publishReviewUpdated(review: Review) {
        val event = ReviewUpdatedEvent(
            reviewId = review.id!!,
            locationId = review.locationId,
            userId = review.userId,
            rating = review.rating.score.toDouble(),
            timestamp = LocalDateTime.now()
        )

        publishEvent(REVIEW_EVENTS_TOPIC, event.reviewId.toString(), event)
        logger.info("Published ReviewUpdatedEvent: reviewId={}", event.reviewId)
    }

    /**
     * 리뷰 삭제 이벤트 발행
     */
    fun publishReviewDeleted(review: Review) {
        val event = ReviewDeletedEvent(
            reviewId = review.id!!,
            locationId = review.locationId,
            userId = review.userId,
            timestamp = LocalDateTime.now()
        )

        publishEvent(REVIEW_EVENTS_TOPIC, event.reviewId.toString(), event)
        logger.info("Published ReviewDeletedEvent: reviewId={}", event.reviewId)
    }

    /**
     * 장소 방문 이벤트 발행
     */
    fun publishLocationVisited(visit: LocationVisit) {
        val event = LocationVisitedEvent(
            visitId = visit.id!!,
            locationId = visit.locationId,
            userId = visit.userId,
            visitedAt = visit.visitedAt,
            visitPurpose = visit.visitPurpose?.name,
            isFavorite = visit.isFavorite,
            timestamp = LocalDateTime.now()
        )

        publishEvent(VISIT_EVENTS_TOPIC, event.visitId.toString(), event)
        logger.info("Published LocationVisitedEvent: visitId={}, locationId={}", event.visitId, event.locationId)
    }

    /**
     * 즐겨찾기 토글 이벤트 발행
     */
    fun publishFavoriteToggled(visit: LocationVisit) {
        val event = FavoriteToggledEvent(
            visitId = visit.id!!,
            locationId = visit.locationId,
            userId = visit.userId,
            isFavorite = visit.isFavorite,
            timestamp = LocalDateTime.now()
        )

        publishEvent(VISIT_EVENTS_TOPIC, event.visitId.toString(), event)
        logger.info("Published FavoriteToggledEvent: locationId={}, userId={}, isFavorite={}",
                   event.locationId, event.userId, event.isFavorite)
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
        val name: String,
        val category: String,
        val createdBy: UUID,
        val coordinates: CoordinatesDto,
        val timestamp: LocalDateTime
    )

    data class LocationUpdatedEvent(
        val locationId: UUID,
        val name: String,
        val category: String,
        val timestamp: LocalDateTime
    )

    data class LocationViewedEvent(
        val locationId: UUID,
        val userId: UUID?,
        val viewCount: Long,
        val timestamp: LocalDateTime
    )

    data class LocationDeactivatedEvent(
        val locationId: UUID,
        val name: String,
        val createdBy: UUID,
        val timestamp: LocalDateTime
    )

    data class ReviewCreatedEvent(
        val reviewId: UUID,
        val locationId: UUID,
        val userId: UUID,
        val rating: Double,
        val hasImages: Boolean,
        val timestamp: LocalDateTime
    )

    data class ReviewUpdatedEvent(
        val reviewId: UUID,
        val locationId: UUID,
        val userId: UUID,
        val rating: Double,
        val timestamp: LocalDateTime
    )

    data class ReviewDeletedEvent(
        val reviewId: UUID,
        val locationId: UUID,
        val userId: UUID,
        val timestamp: LocalDateTime
    )

    data class LocationVisitedEvent(
        val visitId: UUID,
        val locationId: UUID,
        val userId: UUID,
        val visitedAt: LocalDateTime,
        val visitPurpose: String?,
        val isFavorite: Boolean,
        val timestamp: LocalDateTime
    )

    data class FavoriteToggledEvent(
        val visitId: UUID,
        val locationId: UUID,
        val userId: UUID,
        val isFavorite: Boolean,
        val timestamp: LocalDateTime
    )
}