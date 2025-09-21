package com.kangpark.openspot.location.repository.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.location.domain.valueobject.CategoryType
import com.kangpark.openspot.location.domain.valueobject.Coordinates
import com.kangpark.openspot.location.domain.entity.Location
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Location JPA 엔터티
 * Domain 모델과 데이터베이스 테이블 간의 매핑
 */
@Entity
@Table(
    name = "locations",
    schema = "location",
    indexes = [
        Index(name = "idx_location_category", columnList = "category"),
        Index(name = "idx_location_created_by", columnList = "created_by"),
        Index(name = "idx_location_coordinates", columnList = "coordinates")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class LocationJpaEntity(

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "description", length = 1000)
    val description: String? = null,

    @Column(name = "address", length = 200)
    val address: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    val category: CategoryType,

    @Column(name = "latitude", nullable = false)
    val latitude: Double,

    @Column(name = "longitude", nullable = false)
    val longitude: Double,

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    val geometryPoint: Point? = null,

    @Column(name = "created_by", nullable = false)
    val createdBy: UUID,

    @Column(name = "phone_number", length = 20)
    val phoneNumber: String? = null,

    @Column(name = "website_url", length = 500)
    val websiteUrl: String? = null,

    @Column(name = "business_hours", length = 500)
    val businessHours: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "view_count", nullable = false)
    val viewCount: Long = 0L,

    @Column(name = "average_rating", precision = 2, scale = 1)
    val averageRating: BigDecimal? = null,

    @Column(name = "review_count", nullable = false)
    val reviewCount: Long = 0L,

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
        val location = Location(
            name = name,
            description = description,
            address = address,
            category = category,
            coordinates = Coordinates.of(latitude, longitude),
            createdBy = createdBy,
            phoneNumber = phoneNumber,
            websiteUrl = websiteUrl,
            businessHours = businessHours,
            isActive = isActive,
            viewCount = viewCount,
            averageRating = averageRating,
            reviewCount = reviewCount
        )
        // BaseEntity의 id, createdAt, updatedAt 설정
        setBaseEntityFields(location)
        return location
    }

    /**
     * BaseEntity 필드 설정
     */
    private fun setBaseEntityFields(location: Location) {
        // Domain 엔터티의 BaseEntity 필드들을 JPA 엔터티 값으로 설정
        val idField = Location::class.java.superclass.getDeclaredField("id")
        val createdAtField = Location::class.java.superclass.getDeclaredField("createdAt")
        val updatedAtField = Location::class.java.superclass.getDeclaredField("updatedAt")

        idField.isAccessible = true
        createdAtField.isAccessible = true
        updatedAtField.isAccessible = true

        idField.set(location, this.id)
        createdAtField.set(location, this.createdAt)
        updatedAtField.set(location, this.updatedAt)
    }

    companion object {
        /**
         * Domain 모델에서 JPA 엔터티로 변환
         */
        fun fromDomain(location: Location): LocationJpaEntity {
            return LocationJpaEntity(
                name = location.name,
                description = location.description,
                address = location.address,
                category = location.category,
                latitude = location.coordinates.latitude.toDouble(),
                longitude = location.coordinates.longitude.toDouble(),
                createdBy = location.createdBy,
                phoneNumber = location.phoneNumber,
                websiteUrl = location.websiteUrl,
                businessHours = location.businessHours,
                isActive = location.isActive,
                viewCount = location.viewCount,
                averageRating = location.averageRating,
                reviewCount = location.reviewCount
            )
        }

        /**
         * Domain 모델에서 JPA 엔터티로 변환 (업데이트용)
         */
        fun fromDomainWithId(location: Location): LocationJpaEntity {
            // BaseEntity의 필드들 가져오기
            val idField = Location::class.java.superclass.getDeclaredField("id")
            val createdAtField = Location::class.java.superclass.getDeclaredField("createdAt")
            val updatedAtField = Location::class.java.superclass.getDeclaredField("updatedAt")

            idField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true

            val id = idField.get(location) as UUID?
            val createdAt = createdAtField.get(location) as LocalDateTime
            val updatedAt = updatedAtField.get(location) as LocalDateTime

            return LocationJpaEntity(
                id = id,
                name = location.name,
                description = location.description,
                address = location.address,
                category = location.category,
                latitude = location.coordinates.latitude.toDouble(),
                longitude = location.coordinates.longitude.toDouble(),
                createdBy = location.createdBy,
                phoneNumber = location.phoneNumber,
                websiteUrl = location.websiteUrl,
                businessHours = location.businessHours,
                isActive = location.isActive,
                viewCount = location.viewCount,
                averageRating = location.averageRating,
                reviewCount = location.reviewCount,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }

}