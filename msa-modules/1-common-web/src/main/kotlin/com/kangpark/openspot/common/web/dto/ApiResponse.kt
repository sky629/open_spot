package com.kangpark.openspot.common.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.domain.Page
import java.time.LocalDateTime

/**
 * Standard API response wrapper
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }
        
        fun <T> error(error: ErrorResponse): ApiResponse<T> {
            return ApiResponse(success = false, error = error)
        }
    }
}

/**
 * Error response for API errors
 */
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

/**
 * Pageable response wrapper
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: PageInfo
) {
    companion object {
        fun <T> from(pageData: Page<T>): PageResponse<T> {
            return PageResponse(
                content = pageData.content,
                page = PageInfo(
                    number = pageData.number,
                    size = pageData.size,
                    totalElements = pageData.totalElements,
                    totalPages = pageData.totalPages,
                    first = pageData.isFirst,
                    last = pageData.isLast
                )
            )
        }
    }
}

/**
 * Page information
 */
data class PageInfo(
    val number: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)