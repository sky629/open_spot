package com.kangpark.openspot.location.repository.jpa.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.location.domain.vo.Coordinates
import com.kangpark.openspot.location.domain.entity.Location
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * Location JPA 엔터티 (개인 장소 기록)
 * Domain 모델과 데이터베이스 테이블 간의 매핑
 */
@Entity
@Table(
    name = "locations",
    schema = "location",
    indexes = [
        Index(name = "idx_location_user_id", columnList = "user_id"),
        Index(name = "idx_location_category_id", columnList = "category_id"),
        Index(name = "idx_location_group_id", columnList = "group_id"),
        Index(name = "idx_location_coordinates", columnList = "coordinates")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class LocationJpaEntity(

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "description", length = 1000)
    val description: String? = null,

    @Column(name = "address", length = 200)
    val address: String? = null,

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "category_id", nullable = false, columnDefinition = "uuid")
    val categoryId: UUID,

    @Column(name = "latitude", nullable = false)
    val latitude: Double,

    @Column(name = "longitude", nullable = false)
    val longitude: Double,

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    val geometryPoint: Point? = null,

    @Column(name = "icon_url", length = 500)
    val iconUrl: String? = null,

    // 개인 평가 정보
    @Column(name = "rating", columnDefinition = "NUMERIC(2,1)")
    val rating: Double? = null,

    @Column(name = "review", length = 2000)
    val review: String? = null,

    @Column(name = "tags", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    val tags: Array<String> = emptyArray(),

    @Column(name = "is_favorite", nullable = false)
    val isFavorite: Boolean = false,

    // 그룹 관리
    @Column(name = "group_id")
    val groupId: UUID? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

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
    fun toDomain(): Location {
        return Location(
            userId = userId,
            name = name,
            description = description,
            address = address,
            categoryId = categoryId,
            coordinates = Coordinates.of(latitude, longitude),
            iconUrl = iconUrl,
            rating = rating,
            review = review,
            tags = tags.toList(),
            isFavorite = isFavorite,
            groupId = groupId,
            isActive = isActive,
            baseEntity = BaseEntity(
                id = this.id,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt
            )
        )
    }

    companion object {
        /**
         * Domain 모델에서 JPA 엔터티로 변환
         */
        fun fromDomain(location: Location): LocationJpaEntity {
            return LocationJpaEntity(
                id = location.id,
                userId = location.userId,
                name = location.name,
                description = location.description,
                address = location.address,
                categoryId = location.categoryId,
                latitude = location.coordinates.latitude.toDouble(),
                longitude = location.coordinates.longitude.toDouble(),
                iconUrl = location.iconUrl,
                rating = location.rating,
                review = location.review,
                tags = location.tags.toTypedArray(),
                isFavorite = location.isFavorite,
                groupId = location.groupId,
                isActive = location.isActive,
                createdAt = location.createdAt,
                updatedAt = location.updatedAt
            )
        }
    }

}
