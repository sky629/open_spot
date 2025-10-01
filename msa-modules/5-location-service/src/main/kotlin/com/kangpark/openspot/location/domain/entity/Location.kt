package com.kangpark.openspot.location.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.location.domain.vo.Coordinates
import java.time.LocalDateTime
import java.util.*

/**
 * 개인 장소 기록 도메인 엔터티
 * 사용자가 방문한 장소에 대한 개인적인 평점, 리뷰, 메모를 기록하고 관리
 */
data class Location(
    val userId: UUID,                        // 소유자
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val categoryId: UUID,                    // 카테고리 ID (Category 테이블 FK, NOT NULL)
    val coordinates: Coordinates,
    val iconUrl: String? = null,

    // 개인 평가 정보
    val personalRating: Int? = null,         // 1-5 개인 평점
    val personalReview: String? = null,      // 개인 리뷰/메모
    val tags: List<String> = emptyList(),    // 개인 태그
    val isFavorite: Boolean = false,         // 즐겨찾기 여부

    // 그룹 관리
    val groupId: UUID? = null,               // 속한 그룹

    val isActive: Boolean = true,

    // BaseEntity 합성
    val baseEntity: BaseEntity = BaseEntity()
) {

    // 편의 프로퍼티 (BaseEntity 필드 접근)
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt

    /**
     * 개인 평가 업데이트
     */
    fun updatePersonalEvaluation(
        personalRating: Int?,
        personalReview: String?,
        tags: List<String>
    ): Location {
        personalRating?.let {
            require(it in 1..5) { "개인 평점은 1-5 사이의 값이어야 합니다" }
        }
        require(personalReview?.length ?: 0 <= 2000) { "개인 리뷰는 2000자를 초과할 수 없습니다" }
        require(tags.size <= 10) { "태그는 최대 10개까지 등록할 수 있습니다" }
        tags.forEach { tag ->
            require(tag.length <= 20) { "각 태그는 20자를 초과할 수 없습니다" }
        }

        return copy(
            personalRating = personalRating,
            personalReview = personalReview,
            tags = tags,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 장소 기본 정보 업데이트
     */
    fun updateBasicInfo(
        name: String,
        description: String?,
        address: String?,
        categoryId: UUID,
        iconUrl: String?
    ): Location {
        require(name.isNotBlank()) { "장소명은 필수입니다" }
        require(name.length <= 100) { "장소명은 100자를 초과할 수 없습니다" }
        require(description?.length ?: 0 <= 1000) { "설명은 1000자를 초과할 수 없습니다" }
        require(address?.length ?: 0 <= 200) { "주소는 200자를 초과할 수 없습니다" }
        require(iconUrl?.length ?: 0 <= 500) { "아이콘 URL은 500자를 초과할 수 없습니다" }

        return copy(
            name = name,
            description = description,
            address = address,
            categoryId = categoryId,
            iconUrl = iconUrl,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 좌표 업데이트
     */
    fun updateCoordinates(coordinates: Coordinates): Location {
        return copy(
            coordinates = coordinates,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 그룹 변경
     */
    fun changeGroup(newGroupId: UUID?): Location {
        return copy(
            groupId = newGroupId,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 장소 비활성화 (삭제)
     */
    fun deactivate(): Location {
        return copy(
            isActive = false,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 장소 활성화
     */
    fun activate(): Location {
        return copy(
            isActive = true,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 즐겨찾기 토글
     */
    fun toggleFavorite(): Location {
        return copy(
            isFavorite = !isFavorite,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 특정 좌표와의 거리 계산
     */
    fun distanceTo(targetCoordinates: Coordinates): Double {
        return coordinates.distanceTo(targetCoordinates)
    }

    /**
     * 장소 소유자 확인
     */
    fun isOwnedBy(checkUserId: UUID): Boolean {
        return userId == checkUserId
    }

    /**
     * 특정 그룹에 속해있는지 확인
     */
    fun belongsToGroup(checkGroupId: UUID): Boolean {
        return groupId == checkGroupId
    }

    companion object {
        fun create(
            userId: UUID,
            name: String,
            description: String?,
            address: String?,
            categoryId: UUID,
            coordinates: Coordinates,
            iconUrl: String? = null,
            personalRating: Int? = null,
            personalReview: String? = null,
            tags: List<String> = emptyList(),
            groupId: UUID? = null
        ): Location {
            require(name.isNotBlank()) { "장소명은 필수입니다" }
            require(name.length <= 100) { "장소명은 100자를 초과할 수 없습니다" }
            require((description?.length ?: 0) <= 1000) { "설명은 1000자를 초과할 수 없습니다" }
            require((address?.length ?: 0) <= 200) { "주소는 200자를 초과할 수 없습니다" }
            require((iconUrl?.length ?: 0) <= 500) { "아이콘 URL은 500자를 초과할 수 없습니다" }
            personalRating?.let {
                require(it in 1..5) { "개인 평점은 1-5 사이의 값이어야 합니다" }
            }
            require((personalReview?.length ?: 0) <= 2000) { "개인 리뷰는 2000자를 초과할 수 없습니다" }
            require(tags.size <= 10) { "태그는 최대 10개까지 등록할 수 있습니다" }
            tags.forEach { tag ->
                require(tag.length <= 20) { "각 태그는 20자를 초과할 수 없습니다" }
            }

            return Location(
                userId = userId,
                name = name,
                description = description,
                address = address,
                categoryId = categoryId,
                coordinates = coordinates,
                iconUrl = iconUrl,
                personalRating = personalRating,
                personalReview = personalReview,
                tags = tags,
                groupId = groupId
            )
        }
    }
}
