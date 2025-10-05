package com.kangpark.openspot.location.domain.repository

import com.kangpark.openspot.location.domain.entity.Category
import java.util.UUID

/**
 * Category Repository Interface (Domain Layer)
 * 카테고리 저장소 인터페이스
 */
interface CategoryRepository {

    /**
     * 카테고리 저장
     */
    fun save(category: Category): Category

    /**
     * ID로 카테고리 조회
     */
    fun findById(id: UUID): Category?

    /**
     * 여러 ID로 카테고리 배치 조회
     */
    fun findAllById(ids: List<UUID>): List<Category>

    /**
     * 카테고리 코드로 조회
     */
    fun findByCode(code: String): Category?

    /**
     * 활성화된 모든 카테고리 조회 (순서대로)
     */
    fun findAllActiveOrderByDisplayOrder(): List<Category>

    /**
     * 모든 카테고리 조회 (순서대로)
     */
    fun findAllOrderByDisplayOrder(): List<Category>

    /**
     * 카테고리 코드 존재 여부 확인
     */
    fun existsByCode(code: String): Boolean

    /**
     * 카테고리 삭제
     */
    fun deleteById(id: UUID)
}