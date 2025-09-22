package com.kangpark.openspot.location.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.location.domain.vo.VisitPurpose
import java.time.LocalDateTime
import java.util.*

/**
 * 장소 방문 도메인 엔터티
 * 사용자의 장소 방문 기록과 즐겨찾기 정보를 관리
 */
class LocationVisit(
    val locationId: UUID,
    val userId: UUID,
    val visitedAt: LocalDateTime,
    val memo: String? = null,
    val visitDurationMinutes: Int? = null,
    val companionCount: Int? = null,
    val visitPurpose: VisitPurpose? = null,
    val isFavorite: Boolean = false
) : BaseEntity() {

    /**
     * 방문 정보 업데이트
     */
    fun updateVisitInfo(
        memo: String?,
        visitDurationMinutes: Int?,
        companionCount: Int?,
        visitPurpose: VisitPurpose?
    ): LocationVisit {
        require(memo?.length ?: 0 <= 500) { "메모는 500자를 초과할 수 없습니다" }
        require(visitDurationMinutes == null || visitDurationMinutes in 1..1440) {
            "방문 시간은 1분 이상 24시간(1440분) 이하이어야 합니다"
        }
        require(companionCount == null || companionCount in 0..100) {
            "동반자 수는 0명 이상 100명 이하이어야 합니다"
        }

        return LocationVisit(
            locationId = this.locationId,
            userId = this.userId,
            visitedAt = this.visitedAt,
            memo = memo,
            visitDurationMinutes = visitDurationMinutes,
            companionCount = companionCount,
            visitPurpose = visitPurpose,
            isFavorite = this.isFavorite
        )
    }

    /**
     * 즐겨찾기 토글
     */
    fun toggleFavorite(): LocationVisit {
        return LocationVisit(
            locationId = this.locationId,
            userId = this.userId,
            visitedAt = this.visitedAt,
            memo = this.memo,
            visitDurationMinutes = this.visitDurationMinutes,
            companionCount = this.companionCount,
            visitPurpose = this.visitPurpose,
            isFavorite = !this.isFavorite
        )
    }

    /**
     * 즐겨찾기 설정
     */
    fun setFavorite(favorite: Boolean): LocationVisit {
        return LocationVisit(
            locationId = this.locationId,
            userId = this.userId,
            visitedAt = this.visitedAt,
            memo = this.memo,
            visitDurationMinutes = this.visitDurationMinutes,
            companionCount = this.companionCount,
            visitPurpose = this.visitPurpose,
            isFavorite = favorite
        )
    }

    /**
     * 방문자 확인
     */
    fun isVisitedBy(userId: UUID): Boolean {
        return this.userId == userId
    }

    companion object {
        fun create(
            locationId: UUID,
            userId: UUID,
            visitedAt: LocalDateTime = LocalDateTime.now(),
            memo: String? = null,
            visitDurationMinutes: Int? = null,
            companionCount: Int? = null,
            visitPurpose: VisitPurpose? = null
        ): LocationVisit {
            require(memo?.length ?: 0 <= 500) { "메모는 500자를 초과할 수 없습니다" }
            require(visitDurationMinutes == null || visitDurationMinutes in 1..1440) {
                "방문 시간은 1분 이상 24시간(1440분) 이하이어야 합니다"
            }
            require(companionCount == null || companionCount in 0..100) {
                "동반자 수는 0명 이상 100명 이하이어야 합니다"
            }

            return LocationVisit(
                locationId = locationId,
                userId = userId,
                visitedAt = visitedAt,
                memo = memo,
                visitDurationMinutes = visitDurationMinutes,
                companionCount = companionCount,
                visitPurpose = visitPurpose
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocationVisit) return false
        return locationId == other.locationId && userId == other.userId && visitedAt == other.visitedAt
    }

    override fun hashCode(): Int {
        return Objects.hash(locationId, userId, visitedAt)
    }

    override fun toString(): String {
        return "LocationVisit(locationId=$locationId, userId=$userId, visitedAt=$visitedAt, isFavorite=$isFavorite)"
    }
}