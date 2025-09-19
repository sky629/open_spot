package com.kangpark.openspot.location.repository.entity

import com.kangpark.openspot.location.domain.Rating
import com.kangpark.openspot.location.domain.Review
import com.kangpark.openspot.location.domain.ReviewStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Review JPA 엔터티
 * Domain 모델과 데이터베이스 테이블 간의 매핑
 */
@Entity
@Table(
    name = "reviews",
    schema = "location",
    indexes = [
        Index(name = "idx_review_location", columnList = "location_id"),
        Index(name = "idx_review_user", columnList = "user_id"),
        Index(name = "idx_review_rating", columnList = "rating"),
        Index(name = "idx_review_status", columnList = "status")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class ReviewJpaEntity(

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "location_id", nullable = false)
    val locationId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    val rating: BigDecimal,

    @Column(name = "content", nullable = false, length = 2000)
    val content: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ReviewStatus = ReviewStatus.ACTIVE,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "review_images",
        schema = "location",
        joinColumns = [JoinColumn(name = "review_id")]
    )
    @Column(name = "image_url", length = 500)
    val imageUrls: List<String> = emptyList(),

    @Column(name = "helpful_count", nullable = false)
    val helpfulCount: Long = 0L,

    @Column(name = "reported_count", nullable = false)
    val reportedCount: Long = 0L,

    @Column(name = "visited_date")
    val visitedDate: LocalDate? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    /**
     * Domain 모델로 변환
     */
    fun toDomain(): Review {
        val review = Review(
            locationId = locationId,
            userId = userId,
            rating = Rating(rating),
            content = content,
            status = status,
            imageUrls = imageUrls,
            helpfulCount = helpfulCount,
            reportedCount = reportedCount,
            visitedDate = visitedDate
        )
        // BaseEntity의 id, createdAt, updatedAt 설정
        setBaseEntityFields(review)
        return review
    }

    /**
     * BaseEntity 필드 설정
     */
    private fun setBaseEntityFields(review: Review) {
        // Domain 엔터티의 BaseEntity 필드들을 JPA 엔터티 값으로 설정
        val idField = Review::class.java.superclass.getDeclaredField("id")
        val createdAtField = Review::class.java.superclass.getDeclaredField("createdAt")
        val updatedAtField = Review::class.java.superclass.getDeclaredField("updatedAt")

        idField.isAccessible = true
        createdAtField.isAccessible = true
        updatedAtField.isAccessible = true

        idField.set(review, this.id)
        createdAtField.set(review, this.createdAt)
        updatedAtField.set(review, this.updatedAt)
    }

    companion object {
        /**
         * Domain 모델에서 JPA 엔터티로 변환
         */
        fun fromDomain(review: Review): ReviewJpaEntity {
            return ReviewJpaEntity(
                locationId = review.locationId,
                userId = review.userId,
                rating = review.rating.score,
                content = review.content,
                status = review.status,
                imageUrls = review.imageUrls,
                helpfulCount = review.helpfulCount,
                reportedCount = review.reportedCount,
                visitedDate = review.visitedDate
            )
        }

        /**
         * Domain 모델에서 JPA 엔터티로 변환 (업데이트용)
         */
        fun fromDomainWithId(review: Review): ReviewJpaEntity {
            // BaseEntity의 필드들 가져오기
            val idField = Review::class.java.superclass.getDeclaredField("id")
            val createdAtField = Review::class.java.superclass.getDeclaredField("createdAt")
            val updatedAtField = Review::class.java.superclass.getDeclaredField("updatedAt")

            idField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true

            val id = idField.get(review) as UUID?
            val createdAt = createdAtField.get(review) as LocalDateTime
            val updatedAt = updatedAtField.get(review) as LocalDateTime

            return ReviewJpaEntity(
                id = id,
                locationId = review.locationId,
                userId = review.userId,
                rating = review.rating.score,
                content = review.content,
                status = review.status,
                imageUrls = review.imageUrls,
                helpfulCount = review.helpfulCount,
                reportedCount = review.reportedCount,
                visitedDate = review.visitedDate,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}