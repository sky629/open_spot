package com.kangpark.openspot.location.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import java.time.LocalDateTime
import java.util.*

/**
 * 위치 그룹 도메인 엔터티
 * 사용자가 자신의 장소들을 카테고리별로 그룹화하여 관리
 * 예: 맛집, 카페, 데이트 코스, 여행지 등
 */
data class LocationGroup(
    val userId: UUID,
    val name: String,
    val description: String? = null,
    val color: String? = null,              // #FF5722 형식
    val icon: String? = null,                // restaurant, cafe 등
    val displayOrder: Int = 0,               // 표시 순서 (0부터 시작)
    val isShared: Boolean = false,           // 향후 공유 기능용

    // BaseEntity 합성
    val baseEntity: BaseEntity = BaseEntity()
) {

    // 편의 프로퍼티 (BaseEntity 필드 접근)
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt

    /**
     * 그룹 기본 정보 업데이트
     */
    fun updateBasicInfo(
        name: String,
        description: String?,
        color: String?,
        icon: String?
    ): LocationGroup {
        require(name.isNotBlank()) { "그룹명은 필수입니다" }
        require(name.length <= 100) { "그룹명은 100자를 초과할 수 없습니다" }
        require(description?.length ?: 0 <= 500) { "설명은 500자를 초과할 수 없습니다" }
        color?.let {
            require(it.matches(Regex("^#[0-9A-Fa-f]{6}$"))) { "색상은 #RRGGBB 형식이어야 합니다" }
        }
        require(icon?.length ?: 0 <= 50) { "아이콘명은 50자를 초과할 수 없습니다" }

        return copy(
            name = name,
            description = description,
            color = color,
            icon = icon,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 순서 변경
     */
    fun changeOrder(newOrder: Int): LocationGroup {
        require(newOrder >= 0) { "순서는 0 이상이어야 합니다" }

        return copy(
            displayOrder = newOrder,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 공유 상태 변경
     */
    fun toggleShared(): LocationGroup {
        return copy(
            isShared = !isShared,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 그룹 소유자 확인
     */
    fun isOwnedBy(checkUserId: UUID): Boolean {
        return userId == checkUserId
    }

    companion object {
        fun create(
            userId: UUID,
            name: String,
            description: String? = null,
            color: String? = null,
            icon: String? = null,
            displayOrder: Int = 0
        ): LocationGroup {
            require(name.isNotBlank()) { "그룹명은 필수입니다" }
            require(name.length <= 100) { "그룹명은 100자를 초과할 수 없습니다" }
            require(description?.length ?: 0 <= 500) { "설명은 500자를 초과할 수 없습니다" }
            color?.let {
                require(it.matches(Regex("^#[0-9A-Fa-f]{6}$"))) { "색상은 #RRGGBB 형식이어야 합니다" }
            }
            require(icon?.length ?: 0 <= 50) { "아이콘명은 50자를 초과할 수 없습니다" }
            require(displayOrder >= 0) { "순서는 0 이상이어야 합니다" }

            return LocationGroup(
                userId = userId,
                name = name,
                description = description,
                color = color,
                icon = icon,
                displayOrder = displayOrder
            )
        }
    }
}
