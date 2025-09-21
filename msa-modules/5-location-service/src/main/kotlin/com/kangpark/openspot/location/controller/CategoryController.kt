package com.kangpark.openspot.location.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.location.domain.valueobject.CategoryType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Category", description = "카테고리 정보 API")
@RestController
@RequestMapping("/api/v1/categories")
class CategoryController {

    @Operation(summary = "카테고리 목록 조회", description = "사용 가능한 모든 카테고리 목록을 조회합니다.")
    @GetMapping
    fun getCategories(): ResponseEntity<ApiResponse<List<CategoryResponse>>> {
        val categories = CategoryType.values().map { categoryType ->
            CategoryResponse(
                code = categoryType.name,
                displayName = categoryType.displayName,
                description = categoryType.description
            )
        }
        return ResponseEntity.ok(ApiResponse.success(categories))
    }

    data class CategoryResponse(
        val code: String,
        val displayName: String,
        val description: String
    )
}