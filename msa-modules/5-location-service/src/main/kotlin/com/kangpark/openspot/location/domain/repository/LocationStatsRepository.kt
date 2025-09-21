package com.kangpark.openspot.location.domain.repository

import com.kangpark.openspot.location.domain.entity.LocationStats
import com.kangpark.openspot.location.domain.entity.StatsType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

/**
 * LocationStats Domain Repository Interface
 * Infrastructure 레이어에서 구현
 */
interface LocationStatsRepository {

    fun save(locationStats: LocationStats): LocationStats

    fun findById(id: UUID): LocationStats?

    fun deleteById(id: UUID)

    fun findByLocationIdAndStatsDateAndStatsType(
        locationId: UUID,
        statsDate: LocalDate,
        statsType: StatsType
    ): LocationStats?

    fun findByLocationIdAndStatsTypeOrderByStatsDateDesc(
        locationId: UUID,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    fun findByLocationIdAndStatsTypeAndDateRange(
        locationId: UUID,
        statsType: StatsType,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LocationStats>

    fun findByStatsDateAndStatsTypeOrderByViewCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    fun findByStatsDateAndStatsTypeOrderByVisitCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    fun findByStatsDateAndStatsTypeOrderByReviewCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    fun findByStatsDateAndStatsTypeOrderByRatingDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats>

    fun findLatestDailyStatsByLocationId(locationId: UUID): LocationStats?

    fun findLatestMonthlyStatsByLocationId(locationId: UUID): LocationStats?

    fun findLatestYearlyStatsByLocationId(locationId: UUID): LocationStats?

    fun getDailySummary(statsDate: LocalDate): StatsSummary?

    fun getLocationSummaryByDateRange(
        locationId: UUID,
        statsType: StatsType,
        startDate: LocalDate,
        endDate: LocalDate
    ): StatsSummary?

    fun findTopLocationsByViews(
        statsDate: LocalDate,
        statsType: StatsType,
        limit: Int = 10
    ): List<TopLocationStats>

    fun findTopLocationsByVisits(
        statsDate: LocalDate,
        statsType: StatsType,
        limit: Int = 10
    ): List<TopLocationStats>

    fun findPreviousStatsByLocationId(
        locationId: UUID,
        statsType: StatsType,
        currentDate: LocalDate
    ): LocationStats?

    fun existsByLocationIdAndStatsDateAndStatsType(
        locationId: UUID,
        statsDate: LocalDate,
        statsType: StatsType
    ): Boolean

    fun countByStatsTypeAndStatsDateBetween(
        statsType: StatsType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Long

    fun existsById(id: UUID): Boolean

    interface StatsSummary {
        val totalViews: Long?
        val totalVisits: Long?
        val totalReviews: Long?
        val avgRating: BigDecimal?
        val totalFavorites: Long?
        val totalUniqueVisitors: Long?
    }

    interface TopLocationStats {
        val locationId: UUID
        val viewCount: Long
        val visitCount: Long
        val reviewCount: Long
        val averageRating: BigDecimal?
    }
}