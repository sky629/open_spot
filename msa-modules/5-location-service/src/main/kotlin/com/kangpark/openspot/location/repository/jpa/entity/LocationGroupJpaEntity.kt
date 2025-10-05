package com.kangpark.openspot.location.repository.jpa.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.location.domain.entity.LocationGroup
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * LocationGroup JPA 엔터티 (사용자 장소 그룹)
 * Domain 모델과 데이터베이스 테이블 간의 매핑
 */
@Entity
@Table(
    name = "location_groups",
    schema = "location",
    indexes = [
        Index(name = "idx_location_group_user_id", columnList = "user_id"),
        Index(name = "idx_location_group_user_order", columnList = "user_id,display_order")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_location_group_user_name", columnNames = ["user_id", "name"])
    ]
)
@EntityListeners(AuditingEntityListener::class)
class LocationGroupJpaEntity(

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "description", length = 500)
    val description: String? = null,

    @Column(name = "color", length = 7)
    val color: String? = null,

    @Column(name = "icon", length = 50)
    val icon: String? = null,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

    @Column(name = "is_shared", nullable = false)
    val isShared: Boolean = false,

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
    fun toDomain(): LocationGroup {
        return LocationGroup(
            userId = userId,
            name = name,
            description = description,
            color = color,
            icon = icon,
            displayOrder = displayOrder,
            isShared = isShared,
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
        fun fromDomain(locationGroup: LocationGroup): LocationGroupJpaEntity {
            return LocationGroupJpaEntity(
                id = locationGroup.id,
                userId = locationGroup.userId,
                name = locationGroup.name,
                description = locationGroup.description,
                color = locationGroup.color,
                icon = locationGroup.icon,
                displayOrder = locationGroup.displayOrder,
                isShared = locationGroup.isShared,
                createdAt = locationGroup.createdAt,
                updatedAt = locationGroup.updatedAt
            )
        }
    }

}
