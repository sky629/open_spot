package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.domain.entity.ReviewStatus
import com.kangpark.openspot.location.repository.entity.ReviewJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Repository
interface ReviewJpaRepository : JpaRepository<ReviewJpaEntity, UUID> {

    /**
     * 특정 장소의 활성 리뷰 목록 (최신순)
     */
    fun findByLocationIdAndStatusOrderByCreatedAtDesc(
        locationId: UUID,
        status: ReviewStatus,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 특정 장소의 활성 리뷰 목록 (평점순)
     */
    @Query(
        """
        SELECT r FROM ReviewJpaEntity r
        WHERE r.locationId = :locationId
        AND r.status = :status
        ORDER BY r.rating DESC, r.createdAt DESC
        """
    )
    fun findByLocationIdAndStatusOrderByRatingDesc(
        @Param("locationId") locationId: UUID,
        @Param("status") status: ReviewStatus,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 특정 장소의 활성 리뷰 목록 (도움이 되었어요 순)
     */
    fun findByLocationIdAndStatusOrderByHelpfulCountDescCreatedAtDesc(
        locationId: UUID,
        status: ReviewStatus,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 특정 사용자의 리뷰 목록
     */
    fun findByUserIdAndStatusOrderByCreatedAtDesc(
        userId: UUID,
        status: ReviewStatus,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 특정 사용자가 특정 장소에 작성한 리뷰
     */
    fun findByLocationIdAndUserIdAndStatus(
        locationId: UUID,
        userId: UUID,
        status: ReviewStatus
    ): ReviewJpaEntity?

    /**
     * 특정 장소의 평점별 리뷰 개수
     */
    @Query(
        """
        SELECT r.rating as rating, COUNT(r) as count
        FROM ReviewJpaEntity r
        WHERE r.locationId = :locationId
        AND r.status = 'ACTIVE'
        GROUP BY r.rating
        ORDER BY r.rating DESC
        """
    )
    fun countByLocationIdGroupByRating(@Param("locationId") locationId: UUID): List<RatingCount>

    /**
     * 특정 장소의 평균 평점 계산
     */
    @Query(
        """
        SELECT AVG(r.rating)
        FROM ReviewJpaEntity r
        WHERE r.locationId = :locationId
        AND r.status = 'ACTIVE'
        """
    )
    fun calculateAverageRatingByLocationId(@Param("locationId") locationId: UUID): BigDecimal?

    /**
     * 특정 장소의 활성 리뷰 개수
     */
    fun countByLocationIdAndStatus(locationId: UUID, status: ReviewStatus): Long

    /**
     * 특정 사용자의 리뷰 개수
     */
    fun countByUserIdAndStatus(userId: UUID, status: ReviewStatus): Long

    /**
     * 도움이 되었어요 카운트 증가
     */
    @Modifying
    @Query("UPDATE ReviewJpaEntity r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.id = :reviewId")
    fun incrementHelpfulCount(@Param("reviewId") reviewId: UUID): Int

    /**
     * 도움이 되었어요 카운트 감소
     */
    @Modifying
    @Query(
        """
        UPDATE ReviewJpaEntity r
        SET r.helpfulCount = CASE
            WHEN r.helpfulCount > 0 THEN r.helpfulCount - 1
            ELSE 0
        END
        WHERE r.id = :reviewId
        """
    )
    fun decrementHelpfulCount(@Param("reviewId") reviewId: UUID): Int

    /**
     * 신고 카운트 증가
     */
    @Modifying
    @Query("UPDATE ReviewJpaEntity r SET r.reportedCount = r.reportedCount + 1 WHERE r.id = :reviewId")
    fun incrementReportedCount(@Param("reviewId") reviewId: UUID): Int

    /**
     * 특정 평점 이상의 리뷰 목록
     */
    @Query(
        """
        SELECT r FROM ReviewJpaEntity r
        WHERE r.locationId = :locationId
        AND r.status = 'ACTIVE'
        AND r.rating >= :minRating
        ORDER BY r.createdAt DESC
        """
    )
    fun findByLocationIdAndMinRating(
        @Param("locationId") locationId: UUID,
        @Param("minRating") minRating: BigDecimal,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 특정 평점 이하의 리뷰 목록
     */
    @Query(
        """
        SELECT r FROM ReviewJpaEntity r
        WHERE r.locationId = :locationId
        AND r.status = 'ACTIVE'
        AND r.rating <= :maxRating
        ORDER BY r.createdAt DESC
        """
    )
    fun findByLocationIdAndMaxRating(
        @Param("locationId") locationId: UUID,
        @Param("maxRating") maxRating: BigDecimal,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 이미지가 있는 리뷰 목록
     */
    @Query(
        """
        SELECT r FROM ReviewJpaEntity r
        WHERE r.locationId = :locationId
        AND r.status = 'ACTIVE'
        AND SIZE(r.imageUrls) > 0
        ORDER BY r.createdAt DESC
        """
    )
    fun findByLocationIdWithImages(
        @Param("locationId") locationId: UUID,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 최근 리뷰 목록 (전체)
     */
    fun findByStatusOrderByCreatedAtDesc(
        status: ReviewStatus,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 신고가 많은 리뷰 목록 (관리자용)
     */
    @Query(
        """
        SELECT r FROM ReviewJpaEntity r
        WHERE r.reportedCount >= :minReportCount
        ORDER BY r.reportedCount DESC, r.createdAt DESC
        """
    )
    fun findHighlyReportedReviews(
        @Param("minReportCount") minReportCount: Long = 3L,
        pageable: Pageable
    ): Page<ReviewJpaEntity>

    /**
     * 특정 기간 내 리뷰 개수
     */
    @Query(
        """
        SELECT COUNT(r) FROM ReviewJpaEntity r
        WHERE r.locationId = :locationId
        AND r.status = 'ACTIVE'
        AND r.createdAt >= :startDate
        AND r.createdAt <= :endDate
        """
    )
    fun countByLocationIdAndDateRange(
        @Param("locationId") locationId: UUID,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long

    interface RatingCount {
        val rating: BigDecimal
        val count: Long
    }
}