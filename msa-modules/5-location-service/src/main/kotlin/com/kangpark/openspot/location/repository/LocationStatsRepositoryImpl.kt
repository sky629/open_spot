package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.domain.entity.LocationStats
import com.kangpark.openspot.location.domain.entity.StatsType
import com.kangpark.openspot.location.domain.repository.LocationStatsRepository
import com.kangpark.openspot.location.repository.entity.LocationStatsJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

/**
 * LocationStats Repository Implementation
 * Domain Repository 인터페이스를 JPA로 구현
 */
@Repository
class LocationStatsRepositoryImpl(
    private val locationStatsJpaRepository: LocationStatsJpaRepository
) : LocationStatsRepository {

    override fun save(locationStats: LocationStats): LocationStats {
        val jpaEntity = if (locationStats.id != null) {
            LocationStatsJpaEntity.fromDomainWithId(locationStats)
        } else {
            LocationStatsJpaEntity.fromDomain(locationStats)
        }
        val savedEntity = locationStatsJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): LocationStats? {
        return locationStatsJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun deleteById(id: UUID) {
        locationStatsJpaRepository.deleteById(id)
    }

    override fun findByLocationIdAndStatsDateAndStatsType(
        locationId: UUID,
        statsDate: LocalDate,
        statsType: StatsType
    ): LocationStats? {
        return locationStatsJpaRepository.findByLocationIdAndStatsDateAndStatsType(
            locationId, statsDate, statsType
        )?.toDomain()
    }

    override fun findByLocationIdAndStatsTypeOrderByStatsDateDesc(
        locationId: UUID,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats> {
        val jpaPage = locationStatsJpaRepository.findByLocationIdAndStatsTypeOrderByStatsDateDesc(
            locationId, statsType, pageable
        )
        return convertToPageResponse(jpaPage)
    }

    override fun findByLocationIdAndStatsTypeAndDateRange(
        locationId: UUID,
        statsType: StatsType,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LocationStats> {
        return locationStatsJpaRepository.findByLocationIdAndStatsTypeAndDateRange(
            locationId, statsType, startDate, endDate
        ).map { it.toDomain() }
    }

    override fun findByStatsDateAndStatsTypeOrderByViewCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats> {
        val jpaPage = locationStatsJpaRepository.findByStatsDateAndStatsTypeOrderByViewCountDesc(
            statsDate, statsType, pageable
        )
        return convertToPageResponse(jpaPage)
    }

    override fun findByStatsDateAndStatsTypeOrderByVisitCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats> {
        val jpaPage = locationStatsJpaRepository.findByStatsDateAndStatsTypeOrderByVisitCountDesc(
            statsDate, statsType, pageable
        )
        return convertToPageResponse(jpaPage)
    }

    override fun findByStatsDateAndStatsTypeOrderByReviewCountDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats> {
        val jpaPage = locationStatsJpaRepository.findByStatsDateAndStatsTypeOrderByReviewCountDesc(
            statsDate, statsType, pageable
        )
        return convertToPageResponse(jpaPage)
    }

    override fun findByStatsDateAndStatsTypeOrderByRatingDesc(
        statsDate: LocalDate,
        statsType: StatsType,
        pageable: Pageable
    ): Page<LocationStats> {
        val jpaPage = locationStatsJpaRepository.findByStatsDateAndStatsTypeOrderByRatingDesc(
            statsDate, statsType, pageable
        )
        return convertToPageResponse(jpaPage)
    }

    override fun findLatestDailyStatsByLocationId(locationId: UUID): LocationStats? {
        return locationStatsJpaRepository.findLatestDailyStatsByLocationId(locationId)?.toDomain()
    }

    override fun findLatestMonthlyStatsByLocationId(locationId: UUID): LocationStats? {
        return locationStatsJpaRepository.findLatestMonthlyStatsByLocationId(locationId)?.toDomain()
    }

    override fun findLatestYearlyStatsByLocationId(locationId: UUID): LocationStats? {
        return locationStatsJpaRepository.findLatestYearlyStatsByLocationId(locationId)?.toDomain()
    }

    override fun getDailySummary(statsDate: LocalDate): LocationStatsRepository.StatsSummary? {
        return locationStatsJpaRepository.getDailySummary(statsDate)?.let { jpaSummary ->
            object : LocationStatsRepository.StatsSummary {
                override val totalViews: Long? = jpaSummary.totalViews
                override val totalVisits: Long? = jpaSummary.totalVisits
                override val totalReviews: Long? = jpaSummary.totalReviews
                override val avgRating: BigDecimal? = jpaSummary.avgRating
                override val totalFavorites: Long? = jpaSummary.totalFavorites
                override val totalUniqueVisitors: Long? = jpaSummary.totalUniqueVisitors
            }
        }
    }

    override fun getLocationSummaryByDateRange(
        locationId: UUID,
        statsType: StatsType,
        startDate: LocalDate,
        endDate: LocalDate
    ): LocationStatsRepository.StatsSummary? {
        return locationStatsJpaRepository.getLocationSummaryByDateRange(
            locationId, statsType, startDate, endDate
        )?.let { jpaSummary ->
            object : LocationStatsRepository.StatsSummary {
                override val totalViews: Long? = jpaSummary.totalViews
                override val totalVisits: Long? = jpaSummary.totalVisits
                override val totalReviews: Long? = jpaSummary.totalReviews
                override val avgRating: BigDecimal? = jpaSummary.avgRating
                override val totalFavorites: Long? = jpaSummary.totalFavorites
                override val totalUniqueVisitors: Long? = jpaSummary.totalUniqueVisitors
            }
        }
    }

    override fun findTopLocationsByViews(
        statsDate: LocalDate,
        statsType: StatsType,
        limit: Int
    ): List<LocationStatsRepository.TopLocationStats> {
        return locationStatsJpaRepository.findTopLocationsByViews(statsDate, statsType, limit)
            .map { jpaStats ->
                object : LocationStatsRepository.TopLocationStats {
                    override val locationId: UUID = jpaStats.locationId
                    override val viewCount: Long = jpaStats.viewCount
                    override val visitCount: Long = jpaStats.visitCount
                    override val reviewCount: Long = jpaStats.reviewCount
                    override val averageRating: BigDecimal? = jpaStats.averageRating
                }
            }
    }

    override fun findTopLocationsByVisits(
        statsDate: LocalDate,
        statsType: StatsType,
        limit: Int
    ): List<LocationStatsRepository.TopLocationStats> {
        return locationStatsJpaRepository.findTopLocationsByVisits(statsDate, statsType, limit)
            .map { jpaStats ->
                object : LocationStatsRepository.TopLocationStats {
                    override val locationId: UUID = jpaStats.locationId
                    override val viewCount: Long = jpaStats.viewCount
                    override val visitCount: Long = jpaStats.visitCount
                    override val reviewCount: Long = jpaStats.reviewCount
                    override val averageRating: BigDecimal? = jpaStats.averageRating
                }
            }
    }

    override fun findPreviousStatsByLocationId(
        locationId: UUID,
        statsType: StatsType,
        currentDate: LocalDate
    ): LocationStats? {
        return locationStatsJpaRepository.findPreviousStatsByLocationId(
            locationId, statsType, currentDate
        )?.toDomain()
    }

    override fun existsByLocationIdAndStatsDateAndStatsType(
        locationId: UUID,
        statsDate: LocalDate,
        statsType: StatsType
    ): Boolean {
        return locationStatsJpaRepository.existsByLocationIdAndStatsDateAndStatsType(
            locationId, statsDate, statsType
        )
    }

    override fun countByStatsTypeAndStatsDateBetween(
        statsType: StatsType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Long {
        return locationStatsJpaRepository.countByStatsTypeAndStatsDateBetween(
            statsType, startDate, endDate
        )
    }

    override fun existsById(id: UUID): Boolean {
        return locationStatsJpaRepository.existsById(id)
    }

    /**
     * JPA Page를 Domain Page로 변환
     */
    private fun convertToPageResponse(jpaPage: Page<LocationStatsJpaEntity>): Page<LocationStats> {
        val domainContent = jpaPage.content.map { it.toDomain() }
        return PageImpl(domainContent, jpaPage.pageable, jpaPage.totalElements)
    }
}