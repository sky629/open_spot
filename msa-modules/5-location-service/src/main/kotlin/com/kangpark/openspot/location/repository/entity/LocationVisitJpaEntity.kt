package com.kangpark.openspot.location.repository.entity

import com.kangpark.openspot.location.domain.entity.LocationVisit
import com.kangpark.openspot.location.domain.vo.VisitPurpose
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * LocationVisit JPA 엔터티
 * Domain 모델과 데이터베이스 테이블 간의 매핑
 */
@Entity
@Table(
    name = "location_visits",
    schema = "location",
    indexes = [
        Index(name = "idx_visit_location_user", columnList = "location_id, user_id"),
        Index(name = "idx_visit_user", columnList = "user_id"),
        Index(name = "idx_visit_date", columnList = "visited_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_location_user_visit",
            columnNames = ["location_id", "user_id", "visited_at"]
        )
    ]
)
@EntityListeners(AuditingEntityListener::class)
class LocationVisitJpaEntity(

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "location_id", nullable = false)
    val locationId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "visited_at", nullable = false)
    val visitedAt: LocalDateTime,

    @Column(name = "memo", length = 500)
    val memo: String? = null,

    @Column(name = "visit_duration_minutes")
    val visitDurationMinutes: Int? = null,

    @Column(name = "companion_count")
    val companionCount: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_purpose")
    val visitPurpose: VisitPurpose? = null,

    @Column(name = "is_favorite", nullable = false)
    val isFavorite: Boolean = false,

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
    fun toDomain(): LocationVisit {
        val locationVisit = LocationVisit(
            locationId = locationId,
            userId = userId,
            visitedAt = visitedAt,
            memo = memo,
            visitDurationMinutes = visitDurationMinutes,
            companionCount = companionCount,
            visitPurpose = visitPurpose,
            isFavorite = isFavorite
        )
        // BaseEntity의 id, createdAt, updatedAt 설정
        setBaseEntityFields(locationVisit)
        return locationVisit
    }

    /**
     * BaseEntity 필드 설정
     */
    private fun setBaseEntityFields(locationVisit: LocationVisit) {
        // Domain 엔터티의 BaseEntity 필드들을 JPA 엔터티 값으로 설정
        val idField = LocationVisit::class.java.superclass.getDeclaredField("id")
        val createdAtField = LocationVisit::class.java.superclass.getDeclaredField("createdAt")
        val updatedAtField = LocationVisit::class.java.superclass.getDeclaredField("updatedAt")

        idField.isAccessible = true
        createdAtField.isAccessible = true
        updatedAtField.isAccessible = true

        idField.set(locationVisit, this.id)
        createdAtField.set(locationVisit, this.createdAt)
        updatedAtField.set(locationVisit, this.updatedAt)
    }

    companion object {
        /**
         * Domain 모델에서 JPA 엔터티로 변환
         */
        fun fromDomain(locationVisit: LocationVisit): LocationVisitJpaEntity {
            return LocationVisitJpaEntity(
                locationId = locationVisit.locationId,
                userId = locationVisit.userId,
                visitedAt = locationVisit.visitedAt,
                memo = locationVisit.memo,
                visitDurationMinutes = locationVisit.visitDurationMinutes,
                companionCount = locationVisit.companionCount,
                visitPurpose = locationVisit.visitPurpose,
                isFavorite = locationVisit.isFavorite
            )
        }

        /**
         * Domain 모델에서 JPA 엔터티로 변환 (업데이트용)
         */
        fun fromDomainWithId(locationVisit: LocationVisit): LocationVisitJpaEntity {
            // BaseEntity의 필드들 가져오기
            val idField = LocationVisit::class.java.superclass.getDeclaredField("id")
            val createdAtField = LocationVisit::class.java.superclass.getDeclaredField("createdAt")
            val updatedAtField = LocationVisit::class.java.superclass.getDeclaredField("updatedAt")

            idField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true

            val id = idField.get(locationVisit) as UUID?
            val createdAt = createdAtField.get(locationVisit) as LocalDateTime
            val updatedAt = updatedAtField.get(locationVisit) as LocalDateTime

            return LocationVisitJpaEntity(
                id = id,
                locationId = locationVisit.locationId,
                userId = locationVisit.userId,
                visitedAt = locationVisit.visitedAt,
                memo = locationVisit.memo,
                visitDurationMinutes = locationVisit.visitDurationMinutes,
                companionCount = locationVisit.companionCount,
                visitPurpose = locationVisit.visitPurpose,
                isFavorite = locationVisit.isFavorite,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}