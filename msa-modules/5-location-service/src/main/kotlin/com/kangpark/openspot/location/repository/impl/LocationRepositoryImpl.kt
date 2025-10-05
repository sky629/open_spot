package com.kangpark.openspot.location.repository.impl

import com.kangpark.openspot.location.domain.entity.Location
import com.kangpark.openspot.location.domain.repository.LocationRepository
import com.kangpark.openspot.location.repository.jpa.LocationJpaRepository
import com.kangpark.openspot.location.repository.jpa.entity.LocationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
class LocationRepositoryImpl(
    private val locationJpaRepository: LocationJpaRepository
) : LocationRepository {

    override fun save(location: Location): Location {
        val jpaEntity = LocationJpaEntity.fromDomain(location)
        val savedEntity = locationJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): Location? {
        return locationJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun deleteById(id: UUID) {
        locationJpaRepository.deleteById(id)
    }

    override fun findByCoordinatesWithinRadius(
        userId: UUID,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        categoryId: UUID?,
        pageable: Pageable
    ): Page<Location> {
        return if (categoryId != null) {
            locationJpaRepository.findByUserIdAndCategoryIdWithinRadius(
                userId, categoryId, latitude, longitude, radiusMeters, pageable
            )
        } else {
            locationJpaRepository.findByUserIdWithinRadius(
                userId, latitude, longitude, radiusMeters, pageable
            )
        }.map { it.toDomain() }
    }

    override fun findByCoordinatesWithinBounds(
        userId: UUID,
        northEastLat: Double,
        northEastLon: Double,
        southWestLat: Double,
        southWestLon: Double,
        categoryId: UUID?,
        pageable: Pageable
    ): Page<Location> {
        return if (categoryId != null) {
            locationJpaRepository.findByUserIdAndCategoryIdWithinBounds(
                userId, categoryId, northEastLat, northEastLon, southWestLat, southWestLon, pageable
            )
        } else {
            locationJpaRepository.findByUserIdWithinBounds(
                userId, northEastLat, northEastLon, southWestLat, southWestLon, pageable
            )
        }.map { it.toDomain() }
    }

    override fun findByUserIdAndCategoryId(userId: UUID, categoryId: UUID, pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByUserIdAndCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(userId, categoryId, pageable)
            .map { it.toDomain() }
    }

    override fun findByUserIdAndKeyword(userId: UUID, keyword: String, pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByUserIdAndKeywordOrderByCreatedAtDesc(userId, keyword, pageable)
            .map { it.toDomain() }
    }

    override fun findTopRatedLocationsByUser(userId: UUID, pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByUserIdAndIsActiveTrueAndPersonalRatingIsNotNullOrderByPersonalRatingDescCreatedAtDesc(userId, pageable)
            .map { it.toDomain() }
    }

    override fun findRecentLocationsByUser(userId: UUID, pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable)
            .map { it.toDomain() }
    }

    override fun findByUserId(userId: UUID, pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable)
            .map { it.toDomain() }
    }

    override fun findByGroupId(groupId: UUID): List<Location> {
        return locationJpaRepository.findByGroupIdAndIsActiveTrue(groupId)
            .map { it.toDomain() }
    }

    override fun findByUserIdAndGroupId(userId: UUID, groupId: UUID?, pageable: Pageable): Page<Location> {
        return if (groupId == null) {
            locationJpaRepository.findByUserIdAndGroupIdIsNullAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable)
        } else {
            locationJpaRepository.findByUserIdAndGroupIdAndIsActiveTrueOrderByCreatedAtDesc(userId, groupId, pageable)
        }.map { it.toDomain() }
    }

    override fun countByUserIdAndCategoryId(userId: UUID): Map<UUID, Long> {
        return locationJpaRepository.countByUserIdAndCategoryId(userId)
            .associate { it.getCategoryId() to it.getCount() }
    }

    override fun existsById(id: UUID): Boolean {
        return locationJpaRepository.existsById(id)
    }

}