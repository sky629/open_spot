package com.kangpark.openspot.location.repository.jpa.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.location.domain.entity.Category
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

/**
 * Category JPA Entity
 * 카테고리 데이터베이스 매핑 엔티티
 */
@Entity
@Table(
    name = "categories",
    schema = "location",
    indexes = [
        Index(name = "idx_category_code", columnList = "code"),
        Index(name = "idx_category_active", columnList = "is_active")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class CategoryJpaEntity(

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "code", nullable = false, unique = true, length = 50)
    val code: String,

    @Column(name = "display_name", nullable = false, length = 100)
    val displayName: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "icon", length = 50)
    val icon: String? = null,

    @Column(name = "color", length = 7)
    val color: String? = null,

    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,

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
    fun toDomain(): Category {
        return Category(
            code = code,
            displayName = displayName,
            description = description,
            icon = icon,
            color = color,
            displayOrder = displayOrder,
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
         * Domain 모델에서 JPA 엔티티로 변환
         */
        fun fromDomain(category: Category): CategoryJpaEntity {
            return CategoryJpaEntity(
                id = category.id,
                code = category.code,
                displayName = category.displayName,
                description = category.description,
                icon = category.icon,
                color = category.color,
                displayOrder = category.displayOrder,
                isActive = category.isActive,
                createdAt = category.createdAt,
                updatedAt = category.updatedAt
            )
        }
    }
}
