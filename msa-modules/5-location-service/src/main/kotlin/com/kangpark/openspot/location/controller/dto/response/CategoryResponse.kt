package com.kangpark.openspot.location.controller.dto.response

import com.kangpark.openspot.location.domain.entity.Category
import java.util.UUID

/**
 * CategoryResponse
 * 카테고리 정보 응답 DTO
 */
data class CategoryResponse(
    val id: UUID,
    val code: String,
    val displayName: String,
    val description: String?,
    val icon: String?,
    val color: String?,
    val order: Int
) {
    companion object {
        fun from(category: Category): CategoryResponse {
            return CategoryResponse(
                id = category.id,
                code = category.code,
                displayName = category.displayName,
                description = category.description,
                icon = category.icon,
                color = category.color,
                order = category.order
            )
        }
    }
}