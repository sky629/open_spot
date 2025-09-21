package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.domain.entity.Review
import com.kangpark.openspot.location.domain.entity.ReviewStatus
import com.kangpark.openspot.location.domain.repository.ReviewRepository
import com.kangpark.openspot.location.repository.entity.ReviewJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Review Repository Implementation
 * Domain Repository 인터페이스를 JPA로 구현
 */
@Repository
class ReviewRepositoryImpl(
    private val reviewJpaRepository: ReviewJpaRepository
) : ReviewRepository {

    override fun save(review: Review): Review {
        val jpaEntity = if (review.id != null) {
            ReviewJpaEntity.fromDomainWithId(review)
        } else {
            ReviewJpaEntity.fromDomain(review)
        }
        val savedEntity = reviewJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): Review? {
        return reviewJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun deleteById(id: UUID) {
        reviewJpaRepository.deleteById(id)
    }

    override fun findByLocationId(locationId: UUID, pageable: Pageable): Page<Review> {
        val jpaPage = reviewJpaRepository.findByLocationIdAndStatusOrderByCreatedAtDesc(
            locationId = locationId,
            status = ReviewStatus.ACTIVE,
            pageable = pageable
        )
        return convertToPageResponse(jpaPage)
    }

    override fun findByUserId(userId: UUID, pageable: Pageable): Page<Review> {
        val jpaPage = reviewJpaRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
            userId = userId,
            status = ReviewStatus.ACTIVE,
            pageable = pageable
        )
        return convertToPageResponse(jpaPage)
    }

    override fun findByLocationIdAndUserIdAndStatus(
        locationId: UUID,
        userId: UUID,
        status: ReviewStatus
    ): Review? {
        return reviewJpaRepository.findByLocationIdAndUserIdAndStatus(locationId, userId, status)
            ?.toDomain()
    }

    override fun countByLocationIdAndStatus(locationId: UUID, status: ReviewStatus): Long {
        return reviewJpaRepository.countByLocationIdAndStatus(locationId, status)
    }

    override fun existsById(id: UUID): Boolean {
        return reviewJpaRepository.existsById(id)
    }

    /**
     * JPA Page를 Domain Page로 변환
     */
    private fun convertToPageResponse(jpaPage: Page<ReviewJpaEntity>): Page<Review> {
        val domainContent = jpaPage.content.map { it.toDomain() }
        return PageImpl(domainContent, jpaPage.pageable, jpaPage.totalElements)
    }
}