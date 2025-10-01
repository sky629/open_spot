package com.kangpark.openspot.location.repository.impl

import com.kangpark.openspot.location.domain.entity.Category
import com.kangpark.openspot.location.domain.repository.CategoryRepository
import com.kangpark.openspot.location.repository.jpa.CategoryJpaRepository
import com.kangpark.openspot.location.repository.jpa.entity.CategoryJpaEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Category Repository 구현체
 * Domain Layer의 CategoryRepository 인터페이스를 Infrastructure Layer에서 구현
 */
@Repository
@Transactional
class CategoryRepositoryImpl(
    private val categoryJpaRepository: CategoryJpaRepository
) : CategoryRepository {

    override fun save(category: Category): Category {
        val jpaEntity = CategoryJpaEntity.fromDomain(category)
        val savedEntity = categoryJpaRepository.save(jpaEntity)
        return savedEntity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findById(id: UUID): Category? {
        return categoryJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun findAllById(ids: List<UUID>): List<Category> {
        return categoryJpaRepository.findAllById(ids)
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findByCode(code: String): Category? {
        return categoryJpaRepository.findByCode(code)
            ?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAllActiveOrderByOrder(): List<Category> {
        return categoryJpaRepository.findByIsActiveTrueOrderByOrderAsc()
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findAllOrderByOrder(): List<Category> {
        return categoryJpaRepository.findAllByOrderByOrderAsc()
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun existsByCode(code: String): Boolean {
        return categoryJpaRepository.existsByCode(code)
    }

    override fun deleteById(id: UUID) {
        categoryJpaRepository.deleteById(id)
    }
}