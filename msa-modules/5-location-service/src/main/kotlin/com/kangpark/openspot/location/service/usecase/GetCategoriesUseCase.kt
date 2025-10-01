package com.kangpark.openspot.location.service.usecase

import com.kangpark.openspot.location.domain.entity.Category
import com.kangpark.openspot.location.domain.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * GetCategoriesUseCase
 * 카테고리 목록 조회 유스케이스
 */
@Service
@Transactional(readOnly = true)
class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {

    /**
     * 활성화된 모든 카테고리 조회
     */
    fun getActiveCategories(): List<Category> {
        return categoryRepository.findAllActiveOrderByOrder()
    }

    /**
     * 모든 카테고리 조회 (관리자용)
     */
    fun getAllCategories(): List<Category> {
        return categoryRepository.findAllOrderByOrder()
    }
}