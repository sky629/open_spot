package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.domain.CategoryType
import com.kangpark.openspot.location.domain.Location
import com.kangpark.openspot.location.domain.LocationRepository
import com.kangpark.openspot.location.repository.entity.LocationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
        // 새로운 엔터티인지 확인
        val isNew = getIdFromDomain(location) == null

        val jpaEntity = if (isNew) {
            LocationJpaEntity.fromDomain(location)
        } else {
            LocationJpaEntity.fromDomainWithId(location)
        }

        val savedEntity = locationJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): Location? {
        return locationJpaRepository.findById(id)?.toDomain()
    }

    override fun deleteById(id: UUID) {
        locationJpaRepository.deleteById(id)
    }

    override fun findByCoordinatesWithinRadius(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        category: CategoryType?,
        pageable: Pageable
    ): Page<Location> {
        val jpaPage = if (category != null) {
            // 카테고리 필터링이 있는 경우 별도 쿼리 필요
            locationJpaRepository.findByCoordinatesWithinRadius(latitude, longitude, radiusMeters, pageable)
                .filter { it.category == category }
                .let {
                    val content = it.content.filter { entity -> entity.category == category }
                    PageImpl(content, pageable, content.size.toLong())
                }
        } else {
            locationJpaRepository.findByCoordinatesWithinRadius(latitude, longitude, radiusMeters, pageable)
        }

        return jpaPage.map { it.toDomain() }
    }

    override fun findByCategory(category: CategoryType, pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(category, pageable)
            .map { it.toDomain() }
    }

    override fun findByKeyword(keyword: String, pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByKeywordOrderByCreatedAtDesc(keyword, pageable)
            .map { it.toDomain() }
    }

    override fun findPopularLocations(pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByIsActiveTrueOrderByViewCountDescCreatedAtDesc(pageable)
            .map { it.toDomain() }
    }

    override fun findTopRatedLocations(pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByIsActiveTrueAndAverageRatingIsNotNullOrderByAverageRatingDescReviewCountDescCreatedAtDesc(pageable)
            .map { it.toDomain() }
    }

    override fun findRecentLocations(pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
            .map { it.toDomain() }
    }

    override fun findByCreatedBy(createdBy: UUID, pageable: Pageable): Page<Location> {
        return locationJpaRepository.findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(createdBy, pageable)
            .map { it.toDomain() }
    }

    override fun countByCategory(): Map<CategoryType, Long> {
        return locationJpaRepository.countByCategory()
            .associate { it.getCategory() to it.getCount() }
    }

    override fun existsById(id: UUID): Boolean {
        return locationJpaRepository.existsById(id)
    }

    /**
     * Domain Entity에서 ID 추출 (reflection 사용)
     */
    private fun getIdFromDomain(location: Location): UUID? {
        return try {
            val idField = Location::class.java.superclass.getDeclaredField("id")
            idField.isAccessible = true
            idField.get(location) as UUID?
        } catch (e: Exception) {
            null
        }
    }
}