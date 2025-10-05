package com.kangpark.openspot.location.repository.jpa

import com.kangpark.openspot.location.repository.jpa.entity.CategoryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Category JPA Repository
 * Spring Data JPA 인터페이스
 */
@Repository
interface CategoryJpaRepository : JpaRepository<CategoryJpaEntity, UUID> {

    /**
     * 카테고리 코드로 조회
     */
    fun findByCode(code: String): CategoryJpaEntity?

    /**
     * 활성화된 카테고리 목록 조회 (순서대로)
     */
    fun findByIsActiveTrueOrderByDisplayOrderAsc(): List<CategoryJpaEntity>

    /**
     * 모든 카테고리 목록 조회 (순서대로)
     */
    fun findAllByOrderByDisplayOrderAsc(): List<CategoryJpaEntity>

    /**
     * 카테고리 코드 존재 여부 확인
     */
    fun existsByCode(code: String): Boolean
}