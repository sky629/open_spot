package com.kangpark.openspot.location.domain.repository

import com.kangpark.openspot.location.domain.entity.Review
import com.kangpark.openspot.location.domain.entity.ReviewStatus
import com.kangpark.openspot.location.domain.valueobject.Rating
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Review Domain Repository Interface
 * 순수 도메인 레이어의 Repository 인터페이스
 */
interface ReviewRepository {

    /**
     * 리뷰 저장
     */
    fun save(review: Review): Review

    /**
     * 리뷰 조회
     */
    fun findById(id: UUID): Review?

    /**
     * 리뷰 삭제
     */
    fun deleteById(id: UUID)

    /**
     * 특정 장소의 리뷰 목록
     */
    fun findByLocationId(locationId: UUID, pageable: Pageable): Page<Review>

    /**
     * 특정 사용자의 리뷰 목록
     */
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Review>

    /**
     * 특정 장소에 대한 특정 사용자의 리뷰 조회
     */
    fun findByLocationIdAndUserIdAndStatus(
        locationId: UUID,
        userId: UUID,
        status: ReviewStatus
    ): Review?

    /**
     * 특정 장소의 활성 리뷰 개수
     */
    fun countByLocationIdAndStatus(locationId: UUID, status: ReviewStatus): Long

    /**
     * 리뷰 존재 확인
     */
    fun existsById(id: UUID): Boolean
}