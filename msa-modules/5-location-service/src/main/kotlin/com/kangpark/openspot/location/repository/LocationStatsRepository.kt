package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.domain.LocationStats
import com.kangpark.openspot.location.domain.StatsType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface LocationStatsRepository : JpaRepository<LocationStats, UUID> {

    /**
     * 특정 장소의 특정 날짜 통계 조회
     */
    fun findByLocationIdAndStatsDateAndStatsType(
        locationId: UUID,
        statsDate: LocalDate,
        statsType: StatsType
    ): LocationStats?

    /**
     * 특정 장소의 통계 목록 (날짜순)
     */
    fun findByLocationIdAndStatsTypeOrderByStatsDateDesc(
        locationId: UUID,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    /**
     * 특정 기간 내 특정 장소의 통계
     */
    @Query(
        """
        SELECT ls FROM LocationStats ls
        WHERE ls.locationId = :locationId
        AND ls.statsType = :statsType
        AND ls.statsDate >= :startDate
        AND ls.statsDate <= :endDate
        ORDER BY ls.statsDate ASC
        """
    )
    fun findByLocationIdAndStatsTypeAndDateRange(
        @Param("locationId") locationId: UUID,
        @Param("statsType") statsType: StatsType,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<LocationStats>

    /**
     * 특정 날짜의 모든 장소 통계 (조회수 순)
     */
    fun findByStatsDateAndStatsTypeOrderByViewCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    /**
     * 특정 날짜의 모든 장소 통계 (방문수 순)
     */
    fun findByStatsDateAndStatsTypeOrderByVisitCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    /**
     * 특정 날짜의 모든 장소 통계 (리뷰수 순)
     */
    fun findByStatsDateAndStatsTypeOrderByReviewCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    /**
     * 특정 날짜의 모든 장소 통계 (평점 순)
     */
    @Query(
        """
        SELECT ls FROM LocationStats ls
        WHERE ls.statsDate = :statsDate
        AND ls.statsType = :statsType
        AND ls.averageRating IS NOT NULL
        ORDER BY ls.averageRating DESC, ls.reviewCount DESC
        """
    )
    fun findByStatsDateAndStatsTypeOrderByRatingDesc(
        @Param("statsDate") statsDate: LocalDate,
        @Param("statsType") statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    /**
     * 특정 장소의 최신 일별 통계
     */
    @Query(
        """
        SELECT ls FROM LocationStats ls
        WHERE ls.locationId = :locationId
        AND ls.statsType = 'DAILY'
        ORDER BY ls.statsDate DESC
        LIMIT 1
        """
    )
    fun findLatestDailyStatsByLocationId(@Param("locationId") locationId: UUID): LocationStats?

    /**
     * 특정 장소의 최신 월별 통계
     */
    @Query(
        """
        SELECT ls FROM LocationStats ls
        WHERE ls.locationId = :locationId
        AND ls.statsType = 'MONTHLY'
        ORDER BY ls.statsDate DESC
        LIMIT 1
        """
    )
    fun findLatestMonthlyStatsByLocationId(@Param("locationId") locationId: UUID): LocationStats?

    /**
     * 특정 장소의 최신 연별 통계
     */
    @Query(
        """
        SELECT ls FROM LocationStats ls
        WHERE ls.locationId = :locationId
        AND ls.statsType = 'YEARLY'
        ORDER BY ls.statsDate DESC
        LIMIT 1
        """
    )
    fun findLatestYearlyStatsByLocationId(@Param("locationId") locationId: UUID): LocationStats?

    /**
     * 전체 일별 통계 합계
     */
    @Query(
        """
        SELECT
            SUM(ls.viewCount) as totalViews,
            SUM(ls.visitCount) as totalVisits,
            SUM(ls.reviewCount) as totalReviews,
            AVG(ls.averageRating) as avgRating,
            SUM(ls.favoriteCount) as totalFavorites,
            SUM(ls.uniqueVisitorCount) as totalUniqueVisitors
        FROM LocationStats ls
        WHERE ls.statsDate = :statsDate
        AND ls.statsType = 'DAILY'
        """
    )
    fun getDailySummary(@Param("statsDate") statsDate: LocalDate): StatsSummary?

    /**
     * 특정 기간의 총합 통계
     */
    @Query(
        """
        SELECT
            SUM(ls.viewCount) as totalViews,
            SUM(ls.visitCount) as totalVisits,
            SUM(ls.reviewCount) as totalReviews,
            AVG(ls.averageRating) as avgRating,
            SUM(ls.favoriteCount) as totalFavorites,
            SUM(ls.uniqueVisitorCount) as totalUniqueVisitors
        FROM LocationStats ls
        WHERE ls.locationId = :locationId
        AND ls.statsType = :statsType
        AND ls.statsDate >= :startDate
        AND ls.statsDate <= :endDate
        """
    )
    fun getLocationSummaryByDateRange(
        @Param("locationId") locationId: UUID,
        @Param("statsType") statsType: StatsType,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): StatsSummary?

    /**
     * 상위 N개 인기 장소 (특정 날짜, 조회수 기준)
     */
    @Query(
        value = """
        SELECT ls.location_id, ls.view_count, ls.visit_count, ls.review_count, ls.average_rating
        FROM location.location_stats ls
        WHERE ls.stats_date = :statsDate
        AND ls.stats_type = :#{#statsType.name()}
        ORDER BY ls.view_count DESC
        LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findTopLocationsByViews(
        @Param("statsDate") statsDate: LocalDate,
        @Param("statsType") statsType: StatsType,
        @Param("limit") limit: Int = 10
    ): List<TopLocationStats>

    /**
     * 상위 N개 인기 장소 (특정 날짜, 방문수 기준)
     */
    @Query(
        value = """
        SELECT ls.location_id, ls.view_count, ls.visit_count, ls.review_count, ls.average_rating
        FROM location.location_stats ls
        WHERE ls.stats_date = :statsDate
        AND ls.stats_type = :#{#statsType.name()}
        ORDER BY ls.visit_count DESC
        LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findTopLocationsByVisits(
        @Param("statsDate") statsDate: LocalDate,
        @Param("statsType") statsType: StatsType,
        @Param("limit") limit: Int = 10
    ): List<TopLocationStats>

    /**
     * 증가율 계산을 위한 이전 기간 통계
     */
    @Query(
        """
        SELECT ls FROM LocationStats ls
        WHERE ls.locationId = :locationId
        AND ls.statsType = :statsType
        AND ls.statsDate < :currentDate
        ORDER BY ls.statsDate DESC
        LIMIT 1
        """
    )
    fun findPreviousStatsByLocationId(
        @Param("locationId") locationId: UUID,
        @Param("statsType") statsType: StatsType,
        @Param("currentDate") currentDate: LocalDate
    ): LocationStats?

    /**
     * 특정 장소의 통계 존재 여부 확인
     */
    fun existsByLocationIdAndStatsDateAndStatsType(
        locationId: UUID,
        statsDate: LocalDate,
        statsType: StatsType
    ): Boolean

    /**
     * 특정 기간 내 생성된 통계 개수
     */
    fun countByStatsTypeAndStatsDateBetween(
        statsType: StatsType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Long

    interface StatsSummary {
        val totalViews: Long?
        val totalVisits: Long?
        val totalReviews: Long?
        val avgRating: java.math.BigDecimal?
        val totalFavorites: Long?
        val totalUniqueVisitors: Long?
    }

    interface TopLocationStats {
        val locationId: UUID
        val viewCount: Long
        val visitCount: Long
        val reviewCount: Long
        val averageRating: java.math.BigDecimal?
    }
}