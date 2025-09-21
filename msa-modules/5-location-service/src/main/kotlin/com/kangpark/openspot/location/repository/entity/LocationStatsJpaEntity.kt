package com.kangpark.openspot.location.repository.entity

import com.kangpark.openspot.location.domain.entity.LocationStats
import com.kangpark.openspot.location.domain.entity.StatsType
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
 * LocationStats JPA 엔터티
 * Domain 모델과 데이터베이스 테이블 간의 매핑
 */
@Entity
@Table(
    name = "location_stats",
    schema = "location",
    indexes = [
        Index(name = "idx_stats_location", columnList = "location_id"),
        Index(name = "idx_stats_date", columnList = "stats_date"),
        Index(name = "idx_stats_type", columnList = "stats_type")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_location_stats_date_type",
            columnNames = ["location_id", "stats_date", "stats_type"]
        )
    ]
)
@EntityListeners(AuditingEntityListener::class)
class LocationStatsJpaEntity(

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "location_id", nullable = false)
    val locationId: UUID,

    @Column(name = "stats_date", nullable = false)
    val statsDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "stats_type", nullable = false)
    val statsType: StatsType,

    @Column(name = "view_count", nullable = false)
    val viewCount: Long = 0L,

    @Column(name = "visit_count", nullable = false)
    val visitCount: Long = 0L,

    @Column(name = "review_count", nullable = false)
    val reviewCount: Long = 0L,

    @Column(name = "average_rating", precision = 2, scale = 1)
    val averageRating: BigDecimal? = null,

    @Column(name = "favorite_count", nullable = false)
    val favoriteCount: Long = 0L,

    @Column(name = "unique_visitor_count", nullable = false)
    val uniqueVisitorCount: Long = 0L,

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
    fun toDomain(): LocationStats {
        val locationStats = LocationStats(
            locationId = locationId,
            statsDate = statsDate,
            statsType = statsType,
            viewCount = viewCount,
            visitCount = visitCount,
            reviewCount = reviewCount,
            averageRating = averageRating,
            favoriteCount = favoriteCount,
            uniqueVisitorCount = uniqueVisitorCount
        )
        // BaseEntity의 id, createdAt, updatedAt 설정
        setBaseEntityFields(locationStats)
        return locationStats
    }

    /**
     * BaseEntity 필드 설정
     */
    private fun setBaseEntityFields(locationStats: LocationStats) {
        // Domain 엔터티의 BaseEntity 필드들을 JPA 엔터티 값으로 설정
        val idField = LocationStats::class.java.superclass.getDeclaredField("id")
        val createdAtField = LocationStats::class.java.superclass.getDeclaredField("createdAt")
        val updatedAtField = LocationStats::class.java.superclass.getDeclaredField("updatedAt")

        idField.isAccessible = true
        createdAtField.isAccessible = true
        updatedAtField.isAccessible = true

        idField.set(locationStats, this.id)
        createdAtField.set(locationStats, this.createdAt)
        updatedAtField.set(locationStats, this.updatedAt)
    }

    companion object {
        /**
         * Domain 모델에서 JPA 엔터티로 변환
         */
        fun fromDomain(locationStats: LocationStats): LocationStatsJpaEntity {
            return LocationStatsJpaEntity(
                locationId = locationStats.locationId,
                statsDate = locationStats.statsDate,
                statsType = locationStats.statsType,
                viewCount = locationStats.viewCount,
                visitCount = locationStats.visitCount,
                reviewCount = locationStats.reviewCount,
                averageRating = locationStats.averageRating,
                favoriteCount = locationStats.favoriteCount,
                uniqueVisitorCount = locationStats.uniqueVisitorCount
            )
        }

        /**
         * Domain 모델에서 JPA 엔터티로 변환 (업데이트용)
         */
        fun fromDomainWithId(locationStats: LocationStats): LocationStatsJpaEntity {
            // BaseEntity의 필드들 가져오기
            val idField = LocationStats::class.java.superclass.getDeclaredField("id")
            val createdAtField = LocationStats::class.java.superclass.getDeclaredField("createdAt")
            val updatedAtField = LocationStats::class.java.superclass.getDeclaredField("updatedAt")

            idField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true

            val id = idField.get(locationStats) as UUID?
            val createdAt = createdAtField.get(locationStats) as LocalDateTime
            val updatedAt = updatedAtField.get(locationStats) as LocalDateTime

            return LocationStatsJpaEntity(
                id = id,
                locationId = locationStats.locationId,
                statsDate = locationStats.statsDate,
                statsType = locationStats.statsType,
                viewCount = locationStats.viewCount,
                visitCount = locationStats.visitCount,
                reviewCount = locationStats.reviewCount,
                averageRating = locationStats.averageRating,
                favoriteCount = locationStats.favoriteCount,
                uniqueVisitorCount = locationStats.uniqueVisitorCount,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}