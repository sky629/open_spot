package com.kangpark.openspot.location.controller.dto.response

import com.kangpark.openspot.location.domain.entity.Review
import com.kangpark.openspot.location.domain.entity.ReviewStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// Request DTOs
data class CreateReviewRequest(
    @field:NotNull(message = "평점은 필수입니다")
    @field:DecimalMin(value = "1.0", message = "평점은 1점 이상이어야 합니다")
    @field:DecimalMax(value = "5.0", message = "평점은 5점 이하이어야 합니다")
    val rating: BigDecimal,

    @field:NotBlank(message = "리뷰 내용은 필수입니다")
    @field:Size(max = 2000, message = "리뷰 내용은 2000자를 초과할 수 없습니다")
    val content: String,

    val visitedDate: LocalDate? = null,

    @field:Size(max = 5, message = "이미지는 최대 5개까지 등록할 수 있습니다")
    val imageUrls: List<@Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다") String> = emptyList()
)

data class UpdateReviewRequest(
    @field:NotNull(message = "평점은 필수입니다")
    @field:DecimalMin(value = "1.0", message = "평점은 1점 이상이어야 합니다")
    @field:DecimalMax(value = "5.0", message = "평점은 5점 이하이어야 합니다")
    val rating: BigDecimal,

    @field:NotBlank(message = "리뷰 내용은 필수입니다")
    @field:Size(max = 2000, message = "리뷰 내용은 2000자를 초과할 수 없습니다")
    val content: String,

    val visitedDate: LocalDate? = null,

    @field:Size(max = 5, message = "이미지는 최대 5개까지 등록할 수 있습니다")
    val imageUrls: List<@Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다") String> = emptyList()
)

data class ReviewHelpfulRequest(
    @field:NotNull(message = "도움이 되었는지 여부는 필수입니다")
    val isHelpful: Boolean
)

// Response DTOs
data class ReviewResponse(
    val id: UUID,
    val locationId: UUID,
    val userId: UUID,
    val rating: BigDecimal,
    val content: String,
    val status: ReviewStatus,
    val imageUrls: List<String>,
    val helpfulCount: Long,
    val reportedCount: Long,
    val visitedDate: LocalDate?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isEditable: Boolean = false,
    val canVoteHelpful: Boolean = false
) {
    companion object {
        fun from(review: Review, currentUserId: UUID? = null): ReviewResponse {
            return ReviewResponse(
                id = review.id!!,
                locationId = review.locationId,
                userId = review.userId,
                rating = review.rating.score,
                content = review.content,
                status = review.status,
                imageUrls = review.imageUrls.toList(),
                helpfulCount = review.helpfulCount,
                reportedCount = review.reportedCount,
                visitedDate = review.visitedDate,
                createdAt = review.createdAt!!,
                updatedAt = review.updatedAt!!,
                isEditable = currentUserId?.let { review.canBeEditedBy(it) } ?: false,
                canVoteHelpful = currentUserId?.let { it != review.userId && review.isActive() } ?: false
            )
        }
    }
}

data class ReviewSummaryResponse(
    val id: UUID,
    val userId: UUID,
    val rating: BigDecimal,
    val content: String,
    val hasImages: Boolean,
    val helpfulCount: Long,
    val visitedDate: LocalDate?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(review: Review): ReviewSummaryResponse {
            return ReviewSummaryResponse(
                id = review.id!!,
                userId = review.userId,
                rating = review.rating.score,
                content = review.content,
                hasImages = review.imageUrls.isNotEmpty(),
                helpfulCount = review.helpfulCount,
                visitedDate = review.visitedDate,
                createdAt = review.createdAt!!
            )
        }
    }
}

data class ReviewStatsResponse(
    val locationId: UUID,
    val totalReviews: Long,
    val averageRating: BigDecimal?,
    val ratingDistribution: Map<Int, Long>,
    val reviewsWithImagesCount: Long
)

data class RatingDistributionResponse(
    val rating: Int,
    val count: Long,
    val percentage: Double
) {
    companion object {
        fun from(ratingDistribution: Map<Int, Long>): List<RatingDistributionResponse> {
            val totalReviews = ratingDistribution.values.sum()

            return (1..5).map { rating ->
                val count = ratingDistribution[rating] ?: 0L
                val percentage = if (totalReviews > 0) {
                    (count.toDouble() / totalReviews.toDouble()) * 100.0
                } else {
                    0.0
                }

                RatingDistributionResponse(
                    rating = rating,
                    count = count,
                    percentage = String.format("%.1f", percentage).toDouble()
                )
            }.reversed() // 5점부터 1점 순으로 정렬
        }
    }
}

data class ReviewFilterRequest(
    @field:DecimalMin(value = "1.0", message = "최소 평점은 1점 이상이어야 합니다")
    @field:DecimalMax(value = "5.0", message = "최소 평점은 5점 이하이어야 합니다")
    val minRating: BigDecimal? = null,

    @field:DecimalMin(value = "1.0", message = "최대 평점은 1점 이상이어야 합니다")
    @field:DecimalMax(value = "5.0", message = "최대 평점은 5점 이하이어야 합니다")
    val maxRating: BigDecimal? = null,

    val hasImages: Boolean? = null,
    val sortBy: ReviewSortType? = ReviewSortType.NEWEST
)

enum class ReviewSortType {
    NEWEST,    // 최신순
    OLDEST,    // 오래된순
    HIGHEST_RATING,  // 높은 평점순
    LOWEST_RATING,   // 낮은 평점순
    MOST_HELPFUL     // 도움이 많이 된 순
}