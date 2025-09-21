package com.kangpark.openspot.location.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.common.web.dto.PageInfo
import com.kangpark.openspot.common.web.dto.PageResponse
import com.kangpark.openspot.location.controller.dto.response.*
import com.kangpark.openspot.location.domain.valueobject.Rating
import com.kangpark.openspot.location.service.LocationApplicationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Review", description = "리뷰 관리 API")
@RestController
@RequestMapping("/api/v1")
class ReviewController(
    private val locationApplicationService: LocationApplicationService
) {
    private val logger = LoggerFactory.getLogger(ReviewController::class.java)

    @Operation(summary = "리뷰 작성", description = "특정 장소에 대한 리뷰를 작성합니다.")
    @PostMapping("/locations/{locationId}/reviews")
    fun createReview(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Valid @RequestBody request: CreateReviewRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<ReviewResponse>> {
        return try {
            val rating = Rating(request.rating)
            val review = locationApplicationService.createReview(
                locationId = locationId,
                userId = userId,
                rating = rating,
                content = request.content,
                visitedDate = request.visitedDate,
                imageUrls = request.imageUrls
            )

            val response = ReviewResponse.from(review, userId)
            ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid review creation request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<ReviewResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to create review for location: {}", locationId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<ReviewResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "리뷰 상세 조회", description = "특정 리뷰의 상세 정보를 조회합니다.")
    @GetMapping("/reviews/{reviewId}")
    fun getReview(
        @Parameter(description = "리뷰 ID", required = true)
        @PathVariable reviewId: UUID,
        @Parameter(description = "사용자 ID (선택사항)")
        @RequestHeader(value = "X-User-Id", required = false) userId: UUID?
    ): ResponseEntity<ApiResponse<ReviewResponse>> {
        val review = locationApplicationService.getReviewById(reviewId)
            ?: return ResponseEntity.notFound().build()

        val response = ReviewResponse.from(review, userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "특정 장소의 리뷰 목록", description = "특정 장소의 리뷰 목록을 조회합니다.")
    @GetMapping("/locations/{locationId}/reviews")
    fun getReviewsByLocation(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID,
        @Parameter(description = "정렬 방식")
        @RequestParam(defaultValue = "NEWEST") sortBy: ReviewSortType,
        @Parameter(description = "필터 조건")
        @ModelAttribute filter: ReviewFilterRequest,
        @Parameter(description = "사용자 ID (선택사항)")
        @RequestHeader(value = "X-User-Id", required = false) userId: UUID?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<ReviewSummaryResponse>>> {
        val reviewPage = when {
            filter.minRating != null -> {
                locationApplicationService.getReviewsByLocationAndMinRating(
                    locationId, Rating(filter.minRating), pageable
                )
            }
            filter.maxRating != null -> {
                locationApplicationService.getReviewsByLocationAndMaxRating(
                    locationId, Rating(filter.maxRating), pageable
                )
            }
            filter.hasImages == true -> {
                locationApplicationService.getReviewsWithImagesByLocation(locationId, pageable)
            }
            sortBy == ReviewSortType.HIGHEST_RATING -> {
                locationApplicationService.getReviewsByLocationOrderByRating(locationId, pageable)
            }
            sortBy == ReviewSortType.MOST_HELPFUL -> {
                locationApplicationService.getReviewsByLocationOrderByHelpful(locationId, pageable)
            }
            else -> {
                locationApplicationService.getReviewsByLocation(locationId, pageable)
            }
        }

        val responseList = reviewPage.content.map { ReviewSummaryResponse.from(it) }
        val pageResponse = PageResponse(
            content = responseList,
            page = PageInfo(
                number = reviewPage.number,
                size = reviewPage.size,
                totalElements = reviewPage.totalElements,
                totalPages = reviewPage.totalPages,
                first = reviewPage.isFirst,
                last = reviewPage.isLast
            )
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "사용자의 리뷰 목록", description = "특정 사용자가 작성한 리뷰 목록을 조회합니다.")
    @GetMapping("/users/self/reviews")
    fun getMyReviews(
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> {
        val reviewPage = locationApplicationService.getReviewsByUser(userId, pageable)
        val responseList = reviewPage.content.map { ReviewResponse.from(it, userId) }
        val pageResponse = PageResponse(
            content = responseList,
            page = PageInfo(
                number = reviewPage.number,
                size = reviewPage.size,
                totalElements = reviewPage.totalElements,
                totalPages = reviewPage.totalPages,
                first = reviewPage.isFirst,
                last = reviewPage.isLast
            )
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }

    @Operation(summary = "리뷰 수정", description = "자신이 작성한 리뷰를 수정합니다.")
    @PutMapping("/reviews/{reviewId}")
    fun updateReview(
        @Parameter(description = "리뷰 ID", required = true)
        @PathVariable reviewId: UUID,
        @Valid @RequestBody request: UpdateReviewRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<ReviewResponse>> {
        return try {
            val rating = Rating(request.rating)
            val review = locationApplicationService.updateReview(
                reviewId = reviewId,
                userId = userId,
                rating = rating,
                content = request.content,
                visitedDate = request.visitedDate,
                imageUrls = request.imageUrls
            )

            val response = ReviewResponse.from(review, userId)
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid review update request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<ReviewResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to update review: {}", reviewId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<ReviewResponse>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "리뷰 삭제", description = "자신이 작성한 리뷰를 삭제합니다 (논리적 삭제).")
    @DeleteMapping("/reviews/{reviewId}")
    fun deleteReview(
        @Parameter(description = "리뷰 ID", required = true)
        @PathVariable reviewId: UUID,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            locationApplicationService.deleteReview(reviewId, userId)

            val response = mapOf(
                "reviewId" to reviewId,
                "message" to "리뷰가 삭제되었습니다"
            )
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid review deletion request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<Map<String, Any>>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to delete review: {}", reviewId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<Map<String, Any>>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "리뷰 도움이 되었어요", description = "다른 사용자의 리뷰에 도움이 되었다고 표시합니다.")
    @PostMapping("/reviews/{reviewId}/helpful")
    fun toggleReviewHelpful(
        @Parameter(description = "리뷰 ID", required = true)
        @PathVariable reviewId: UUID,
        @Valid @RequestBody request: ReviewHelpfulRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val review = locationApplicationService.toggleHelpful(reviewId, userId, request.isHelpful)

            val response = mapOf(
                "reviewId" to reviewId,
                "helpfulCount" to review.helpfulCount,
                "message" to if (request.isHelpful) "도움이 되었다고 표시했습니다" else "도움이 되지 않는다고 표시했습니다"
            )
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid helpful toggle request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<Map<String, Any>>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to toggle review helpful: {}", reviewId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<Map<String, Any>>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "리뷰 신고", description = "부적절한 리뷰를 신고합니다.")
    @PostMapping("/reviews/{reviewId}/report")
    fun reportReview(
        @Parameter(description = "리뷰 ID", required = true)
        @PathVariable reviewId: UUID,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            val review = locationApplicationService.reportReview(reviewId, userId)

            val response = mapOf(
                "reviewId" to reviewId,
                "reportedCount" to review.reportedCount,
                "message" to "리뷰가 신고되었습니다"
            )
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid review report request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error<Map<String, Any>>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to report review: {}", reviewId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error<Map<String, Any>>(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "장소의 리뷰 통계", description = "특정 장소의 리뷰 통계 정보를 조회합니다.")
    @GetMapping("/locations/{locationId}/reviews/stats")
    fun getReviewStats(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID
    ): ResponseEntity<ApiResponse<ReviewStatsResponse>> {
        val stats = locationApplicationService.getReviewStats(locationId)
        val response = ReviewStatsResponse(
            locationId = stats.locationId,
            totalReviews = stats.totalReviews,
            averageRating = stats.averageRating?.score,
            ratingDistribution = stats.ratingDistribution,
            reviewsWithImagesCount = stats.reviewsWithImagesCount
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "장소의 평점 분포", description = "특정 장소의 평점별 리뷰 분포를 조회합니다.")
    @GetMapping("/locations/{locationId}/reviews/rating-distribution")
    fun getRatingDistribution(
        @Parameter(description = "장소 ID", required = true)
        @PathVariable locationId: UUID
    ): ResponseEntity<ApiResponse<List<RatingDistributionResponse>>> {
        val ratingDistribution = locationApplicationService.getRatingDistributionByLocation(locationId)
        val response = RatingDistributionResponse.from(ratingDistribution)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "최근 리뷰 목록", description = "전체 최근 리뷰 목록을 조회합니다.")
    @GetMapping("/reviews/recent")
    fun getRecentReviews(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<PageResponse<ReviewSummaryResponse>>> {
        val reviewPage = locationApplicationService.getRecentReviews(pageable)
        val responseList = reviewPage.content.map { ReviewSummaryResponse.from(it) }
        val pageResponse = PageResponse(
            content = responseList,
            page = PageInfo(
                number = reviewPage.number,
                size = reviewPage.size,
                totalElements = reviewPage.totalElements,
                totalPages = reviewPage.totalPages,
                first = reviewPage.isFirst,
                last = reviewPage.isLast
            )
        )
        return ResponseEntity.ok(ApiResponse.success(pageResponse))
    }
}