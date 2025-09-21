package com.kangpark.openspot.location.repository

import com.kangpark.openspot.location.domain.entity.LocationVisit
import com.kangpark.openspot.location.domain.repository.LocationVisitRepository
import com.kangpark.openspot.location.repository.entity.LocationVisitJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

/**
 * LocationVisit Repository Implementation
 * Domain Repository 인터페이스를 JPA로 구현
 */
@Repository
class LocationVisitRepositoryImpl(
    private val locationVisitJpaRepository: LocationVisitJpaRepository
) : LocationVisitRepository {

    override fun save(locationVisit: LocationVisit): LocationVisit {
        val jpaEntity = if (locationVisit.id != null) {
            LocationVisitJpaEntity.fromDomainWithId(locationVisit)
        } else {
            LocationVisitJpaEntity.fromDomain(locationVisit)
        }
        val savedEntity = locationVisitJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): LocationVisit? {
        return locationVisitJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun deleteById(id: UUID) {
        locationVisitJpaRepository.deleteById(id)
    }

    override fun findByUserId(userId: UUID, pageable: Pageable): Page<LocationVisit> {
        val jpaPage = locationVisitJpaRepository.findByUserIdOrderByVisitedAtDesc(userId, pageable)
        return convertToPageResponse(jpaPage)
    }

    override fun findByLocationId(locationId: UUID, pageable: Pageable): Page<LocationVisit> {
        val jpaPage = locationVisitJpaRepository.findByLocationIdOrderByVisitedAtDesc(locationId, pageable)
        return convertToPageResponse(jpaPage)
    }

    override fun findByUserIdAndLocationId(userId: UUID, locationId: UUID, pageable: Pageable): Page<LocationVisit> {
        val jpaPage = locationVisitJpaRepository.findByLocationIdAndUserIdOrderByVisitedAtDesc(locationId, userId, pageable)
        return convertToPageResponse(jpaPage)
    }

    override fun findByUserIdAndVisitedAtBetween(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<LocationVisit> {
        val jpaPage = locationVisitJpaRepository.findRecentVisitsByUser(userId, pageable)
        // 필터링 로직은 별도 구현 필요
        return convertToPageResponse(jpaPage)
    }

    override fun countByLocationId(locationId: UUID): Long {
        return locationVisitJpaRepository.countByLocationId(locationId)
    }

    override fun existsById(id: UUID): Boolean {
        return locationVisitJpaRepository.existsById(id)
    }

    /**
     * JPA Page를 Domain Page로 변환
     */
    private fun convertToPageResponse(jpaPage: Page<LocationVisitJpaEntity>): Page<LocationVisit> {
        val domainContent = jpaPage.content.map { it.toDomain() }
        return PageImpl(domainContent, jpaPage.pageable, jpaPage.totalElements)
    }
}