package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.repository.LocationRepository
import com.kangpark.openspot.location.domain.vo.Rating
import com.kangpark.openspot.location.domain.entity.Review
import com.kangpark.openspot.location.domain.entity.ReviewStatus
import com.kangpark.openspot.location.domain.repository.ReviewRepository
import com.kangpark.openspot.location.service.event.LocationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

/**
 * 리뷰 생성 Use Case
 */
@Component
@Transactional
class CreateReviewUseCase(
    private val reviewRepository: ReviewRepository,
    private val locationRepository: LocationRepository,
    private val locationEventPublisher: LocationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(CreateReviewUseCase::class.java)

    /**
     * 새로운 리뷰를 생성합니다.
     */
    fun execute(
        locationId: UUID,
        userId: UUID,
        rating: Rating,
        content: String,
        visitedDate: LocalDate? = null,
        imageUrls: List<String> = emptyList()
    ): Review {
        logger.info("Creating review: locationId={}, userId={}, rating={}", locationId, userId, rating.score)

        // 장소 존재 및 활성 상태 확인
        val location = locationRepository.findById(locationId)
            ?: throw IllegalArgumentException("Location not found: $locationId")
        require(location.isActive) { "Cannot review inactive location" }

        // 기존 리뷰 중복 확인 (한 사용자가 같은 장소에 여러 리뷰 금지)
        val existingReview = reviewRepository.findByLocationIdAndUserIdAndStatus(
            locationId, userId, ReviewStatus.ACTIVE
        )
        require(existingReview == null) { "User already has an active review for this location" }

        // 리뷰 생성
        val review = Review.create(
            locationId = locationId,
            userId = userId,
            rating = rating,
            content = content,
            imageUrls = imageUrls,
            visitedDate = visitedDate
        )

        val savedReview = reviewRepository.save(review)

        // 리뷰 생성 이벤트 발행
        locationEventPublisher.publishReviewCreated(savedReview)

        logger.info("Review created successfully: id={}", savedReview.id)
        return savedReview
    }
}