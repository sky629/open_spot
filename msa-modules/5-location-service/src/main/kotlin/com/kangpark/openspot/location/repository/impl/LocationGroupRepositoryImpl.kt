package com.kangpark.openspot.location.repository.impl

import com.kangpark.openspot.location.domain.entity.LocationGroup
import com.kangpark.openspot.location.domain.repository.LocationGroupRepository
import com.kangpark.openspot.location.repository.jpa.LocationGroupJpaRepository
import com.kangpark.openspot.location.repository.jpa.entity.LocationGroupJpaEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * LocationGroup Repository 구현체
 */
@Repository
@Transactional(readOnly = true)
class LocationGroupRepositoryImpl(
    private val jpaRepository: LocationGroupJpaRepository
) : LocationGroupRepository {

    @Transactional
    override fun save(locationGroup: LocationGroup): LocationGroup {
        val jpaEntity = LocationGroupJpaEntity.fromDomain(locationGroup)
        val saved = jpaRepository.save(jpaEntity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): LocationGroup? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserIdOrderByDisplayOrder(userId: UUID): List<LocationGroup> {
        return jpaRepository.findByUserIdOrderByDisplayOrderAsc(userId)
            .map { it.toDomain() }
    }

    override fun existsByUserIdAndName(userId: UUID, name: String): Boolean {
        return jpaRepository.existsByUserIdAndName(userId, name)
    }

    override fun findMaxOrderByUserId(userId: UUID): Int? {
        return jpaRepository.findMaxOrderByUserId(userId)
    }

    @Transactional
    override fun delete(locationGroup: LocationGroup) {
        jpaRepository.deleteById(locationGroup.id)
    }

    @Transactional
    override fun updateOrders(groups: List<LocationGroup>): List<LocationGroup> {
        val jpaEntities = groups.map { LocationGroupJpaEntity.fromDomain(it) }
        val saved = jpaRepository.saveAll(jpaEntities)
        return saved.map { it.toDomain() }
    }
}