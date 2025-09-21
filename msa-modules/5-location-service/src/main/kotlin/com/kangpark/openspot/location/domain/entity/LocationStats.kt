package com.kangpark.openspot.location.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

/**
 * 장소 통계 도메인 엔터티
 * 장소별 통계 정보를 관리 (일별, 월별, 연별)
 */
class LocationStats(
    val locationId: UUID,
    val statsDate: LocalDate,
    val statsType: StatsType,
    val viewCount: Long = 0L,
    val visitCount: Long = 0L,
    val reviewCount: Long = 0L,
    val averageRating: BigDecimal? = null,
    val favoriteCount: Long = 0L,
    val uniqueVisitorCount: Long = 0L
) : BaseEntity() {

    /**
     * 통계 업데이트
     */
    fun updateStats(
        viewCount: Long,
        visitCount: Long,
        reviewCount: Long,
        averageRating: BigDecimal?,
        favoriteCount: Long,
        uniqueVisitorCount: Long
    ): LocationStats {
        require(viewCount >= 0) { "조회수는 0 이상이어야 합니다" }
        require(visitCount >= 0) { "방문수는 0 이상이어야 합니다" }
        require(reviewCount >= 0) { "리뷰수는 0 이상이어야 합니다" }
        require(favoriteCount >= 0) { "즐겨찾기수는 0 이상이어야 합니다" }
        require(uniqueVisitorCount >= 0) { "고유 방문자수는 0 이상이어야 합니다" }

        return LocationStats(
            locationId = this.locationId,
            statsDate = this.statsDate,
            statsType = this.statsType,
            viewCount = viewCount,
            visitCount = visitCount,
            reviewCount = reviewCount,
            averageRating = averageRating,
            favoriteCount = favoriteCount,
            uniqueVisitorCount = uniqueVisitorCount
        )
    }

    /**
     * 조회수 증가
     */
    fun incrementViewCount(count: Long = 1L): LocationStats {
        require(count > 0) { "증가 수는 0보다 커야 합니다" }

        return LocationStats(
            locationId = this.locationId,
            statsDate = this.statsDate,
            statsType = this.statsType,
            viewCount = this.viewCount + count,
            visitCount = this.visitCount,
            reviewCount = this.reviewCount,
            averageRating = this.averageRating,
            favoriteCount = this.favoriteCount,
            uniqueVisitorCount = this.uniqueVisitorCount
        )
    }

    /**
     * 방문수 증가
     */
    fun incrementVisitCount(count: Long = 1L): LocationStats {
        require(count > 0) { "증가 수는 0보다 커야 합니다" }

        return LocationStats(
            locationId = this.locationId,
            statsDate = this.statsDate,
            statsType = this.statsType,
            viewCount = this.viewCount,
            visitCount = this.visitCount + count,
            reviewCount = this.reviewCount,
            averageRating = this.averageRating,
            favoriteCount = this.favoriteCount,
            uniqueVisitorCount = this.uniqueVisitorCount
        )
    }

    /**
     * 리뷰수 증가/감소
     */
    fun updateReviewCount(delta: Long): LocationStats {
        val newReviewCount = maxOf(0L, this.reviewCount + delta)

        return LocationStats(
            locationId = this.locationId,
            statsDate = this.statsDate,
            statsType = this.statsType,
            viewCount = this.viewCount,
            visitCount = this.visitCount,
            reviewCount = newReviewCount,
            averageRating = this.averageRating,
            favoriteCount = this.favoriteCount,
            uniqueVisitorCount = this.uniqueVisitorCount
        )
    }

    /**
     * 즐겨찾기 수 증가/감소
     */
    fun updateFavoriteCount(delta: Long): LocationStats {
        val newFavoriteCount = maxOf(0L, this.favoriteCount + delta)

        return LocationStats(
            locationId = this.locationId,
            statsDate = this.statsDate,
            statsType = this.statsType,
            viewCount = this.viewCount,
            visitCount = this.visitCount,
            reviewCount = this.reviewCount,
            averageRating = this.averageRating,
            favoriteCount = newFavoriteCount,
            uniqueVisitorCount = this.uniqueVisitorCount
        )
    }

    /**
     * 고유 방문자 수 설정
     */
    fun setUniqueVisitorCount(count: Long): LocationStats {
        require(count >= 0) { "고유 방문자수는 0 이상이어야 합니다" }

        return LocationStats(
            locationId = this.locationId,
            statsDate = this.statsDate,
            statsType = this.statsType,
            viewCount = this.viewCount,
            visitCount = this.visitCount,
            reviewCount = this.reviewCount,
            averageRating = this.averageRating,
            favoriteCount = this.favoriteCount,
            uniqueVisitorCount = count
        )
    }

    /**
     * 평점 업데이트
     */
    fun updateAverageRating(averageRating: BigDecimal?): LocationStats {
        return LocationStats(
            locationId = this.locationId,
            statsDate = this.statsDate,
            statsType = this.statsType,
            viewCount = this.viewCount,
            visitCount = this.visitCount,
            reviewCount = this.reviewCount,
            averageRating = averageRating,
            favoriteCount = this.favoriteCount,
            uniqueVisitorCount = this.uniqueVisitorCount
        )
    }

    companion object {
        fun createDaily(
            locationId: UUID,
            date: LocalDate = LocalDate.now()
        ): LocationStats {
            return LocationStats(
                locationId = locationId,
                statsDate = date,
                statsType = StatsType.DAILY
            )
        }

        fun createMonthly(
            locationId: UUID,
            yearMonth: YearMonth = YearMonth.now()
        ): LocationStats {
            return LocationStats(
                locationId = locationId,
                statsDate = yearMonth.atDay(1),
                statsType = StatsType.MONTHLY
            )
        }

        fun createYearly(
            locationId: UUID,
            year: Int = LocalDate.now().year
        ): LocationStats {
            return LocationStats(
                locationId = locationId,
                statsDate = LocalDate.of(year, 1, 1),
                statsType = StatsType.YEARLY
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocationStats) return false
        return locationId == other.locationId && statsDate == other.statsDate && statsType == other.statsType
    }

    override fun hashCode(): Int {
        return Objects.hash(locationId, statsDate, statsType)
    }

    override fun toString(): String {
        return "LocationStats(locationId=$locationId, statsDate=$statsDate, statsType=$statsType)"
    }
}

enum class StatsType {
    DAILY,    // 일별 통계
    MONTHLY,  // 월별 통계
    YEARLY    // 연별 통계
}