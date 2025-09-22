package com.kangpark.openspot.location.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.location.domain.vo.Rating
import java.time.LocalDate
import java.util.*

/**
 * 리뷰 도메인 엔터티
 * 사용자가 장소에 대해 작성하는 리뷰 정보를 관리
 */
class Review(
    val locationId: UUID,
    val userId: UUID,
    val rating: Rating,
    val content: String,
    val status: ReviewStatus = ReviewStatus.ACTIVE,
    val imageUrls: List<String> = emptyList(),
    val helpfulCount: Long = 0L,
    val reportedCount: Long = 0L,
    val visitedDate: LocalDate? = null
) : BaseEntity() {

    /**
     * 리뷰 수정
     */
    fun updateReview(
        rating: Rating,
        content: String,
        visitedDate: LocalDate? = null,
        imageUrls: List<String> = this.imageUrls
    ): Review {
        require(content.isNotBlank()) { "리뷰 내용은 필수입니다" }
        require(content.length <= 2000) { "리뷰 내용은 2000자를 초과할 수 없습니다" }
        require(imageUrls.size <= 5) { "리뷰당 최대 5개의 이미지만 등록할 수 있습니다" }

        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = rating,
            content = content,
            status = this.status,
            imageUrls = imageUrls,
            helpfulCount = this.helpfulCount,
            reportedCount = this.reportedCount,
            visitedDate = visitedDate
        )
    }

    /**
     * 이미지 추가
     */
    fun addImage(imageUrl: String): Review {
        require(imageUrls.size < 5) { "리뷰당 최대 5개의 이미지만 등록할 수 있습니다" }
        require(imageUrl.isNotBlank()) { "이미지 URL은 필수입니다" }

        val updatedImageUrls = imageUrls.toMutableList().apply { add(imageUrl) }

        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = this.status,
            imageUrls = updatedImageUrls,
            helpfulCount = this.helpfulCount,
            reportedCount = this.reportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 이미지 제거
     */
    fun removeImage(imageUrl: String): Review {
        val updatedImageUrls = imageUrls.toMutableList().apply { remove(imageUrl) }

        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = this.status,
            imageUrls = updatedImageUrls,
            helpfulCount = this.helpfulCount,
            reportedCount = this.reportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 모든 이미지 제거
     */
    fun clearImages(): Review {
        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = this.status,
            imageUrls = emptyList(),
            helpfulCount = this.helpfulCount,
            reportedCount = this.reportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 도움이 되었어요 카운트 증가
     */
    fun incrementHelpfulCount(): Review {
        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = this.status,
            imageUrls = this.imageUrls,
            helpfulCount = this.helpfulCount + 1,
            reportedCount = this.reportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 도움이 되었어요 카운트 감소
     */
    fun decrementHelpfulCount(): Review {
        val newCount = if (this.helpfulCount > 0) this.helpfulCount - 1 else 0

        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = this.status,
            imageUrls = this.imageUrls,
            helpfulCount = newCount,
            reportedCount = this.reportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 신고 카운트 증가
     */
    fun incrementReportedCount(): Review {
        val newReportedCount = this.reportedCount + 1
        val newStatus = if (newReportedCount >= 5) ReviewStatus.HIDDEN else this.status

        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = newStatus,
            imageUrls = this.imageUrls,
            helpfulCount = this.helpfulCount,
            reportedCount = newReportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 리뷰 숨김 처리
     */
    fun hide(): Review {
        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = ReviewStatus.HIDDEN,
            imageUrls = this.imageUrls,
            helpfulCount = this.helpfulCount,
            reportedCount = this.reportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 리뷰 활성화
     */
    fun activate(): Review {
        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = ReviewStatus.ACTIVE,
            imageUrls = this.imageUrls,
            helpfulCount = this.helpfulCount,
            reportedCount = this.reportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 리뷰 삭제 (논리적 삭제)
     */
    fun delete(): Review {
        return Review(
            locationId = this.locationId,
            userId = this.userId,
            rating = this.rating,
            content = this.content,
            status = ReviewStatus.DELETED,
            imageUrls = this.imageUrls,
            helpfulCount = this.helpfulCount,
            reportedCount = this.reportedCount,
            visitedDate = this.visitedDate
        )
    }

    /**
     * 리뷰가 활성 상태인지 확인
     */
    fun isActive(): Boolean {
        return status == ReviewStatus.ACTIVE
    }

    /**
     * 리뷰가 수정 가능한지 확인 (작성자 본인만)
     */
    fun canBeEditedBy(userId: UUID): Boolean {
        return this.userId == userId && isActive()
    }

    /**
     * 리뷰 작성자 확인
     */
    fun isWrittenBy(userId: UUID): Boolean {
        return this.userId == userId
    }

    companion object {
        fun create(
            locationId: UUID,
            userId: UUID,
            rating: Rating,
            content: String,
            visitedDate: LocalDate? = null,
            imageUrls: List<String> = emptyList()
        ): Review {
            require(content.isNotBlank()) { "리뷰 내용은 필수입니다" }
            require(content.length <= 2000) { "리뷰 내용은 2000자를 초과할 수 없습니다" }
            require(imageUrls.size <= 5) { "리뷰당 최대 5개의 이미지만 등록할 수 있습니다" }

            return Review(
                locationId = locationId,
                userId = userId,
                rating = rating,
                content = content,
                visitedDate = visitedDate,
                imageUrls = imageUrls
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Review) return false
        return locationId == other.locationId && userId == other.userId
    }

    override fun hashCode(): Int {
        return Objects.hash(locationId, userId)
    }

    override fun toString(): String {
        return "Review(locationId=$locationId, userId=$userId, rating=$rating, status=$status)"
    }
}

enum class ReviewStatus {
    ACTIVE,    // 활성 상태
    HIDDEN,    // 숨김 처리 (관리자에 의해 또는 자동)
    DELETED    // 삭제 처리 (사용자에 의해)
}