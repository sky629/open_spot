package com.kangpark.openspot.location.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import java.time.LocalDateTime
import java.util.*

/**
 * Category Domain Entity
 * 장소 카테고리 도메인 엔티티 (음식점, 카페, 쇼핑 등)
 */
data class Category(
    val code: String,
    val displayName: String,
    val description: String?,
    val icon: String?,
    val color: String?,
    val order: Int,
    val isActive: Boolean = true,

    // BaseEntity 합성
    val baseEntity: BaseEntity = BaseEntity()
) {

    // 편의 프로퍼티 (BaseEntity 필드 접근)
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt

    init {
        require(code.isNotBlank()) { "카테고리 코드는 필수입니다" }
        require(displayName.isNotBlank()) { "카테고리 표시명은 필수입니다" }
        require(order >= 0) { "카테고리 순서는 0 이상이어야 합니다" }
        color?.let {
            require(it.matches(Regex("^#[0-9A-Fa-f]{6}$"))) { "색상은 #RRGGBB 형식이어야 합니다" }
        }
    }

    /**
     * 카테고리 활성화
     */
    fun activate(): Category {
        return copy(
            isActive = true,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 카테고리 비활성화
     */
    fun deactivate(): Category {
        return copy(
            isActive = false,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 카테고리 정보 수정
     */
    fun update(
        displayName: String? = null,
        description: String? = null,
        icon: String? = null,
        color: String? = null,
        order: Int? = null
    ): Category {
        return copy(
            displayName = displayName ?: this.displayName,
            description = description ?: this.description,
            icon = icon ?: this.icon,
            color = color ?: this.color,
            order = order ?: this.order,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    companion object {
        /**
         * 새 카테고리 생성
         */
        fun create(
            code: String,
            displayName: String,
            description: String? = null,
            icon: String? = null,
            color: String? = null,
            order: Int = 0
        ): Category {
            return Category(
                code = code,
                displayName = displayName,
                description = description,
                icon = icon,
                color = color,
                order = order,
                isActive = true
            )
        }
    }
}
