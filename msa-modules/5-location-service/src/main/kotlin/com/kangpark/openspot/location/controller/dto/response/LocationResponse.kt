package com.kangpark.openspot.location.controller.dto.response

import com.kangpark.openspot.location.domain.entity.*
import com.kangpark.openspot.location.domain.vo.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// Response DTOs
data class LocationResponse(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val description: String?,
    val address: String?,
    val categoryId: UUID,
    val category: CategoryInfo,
    val coordinates: CoordinatesResponse,
    val iconUrl: String?,
    val rating: Double?,
    val review: String?,
    val tags: List<String>,
    val groupId: UUID?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val distance: Double? = null
) {
    companion object {
        fun from(location: Location, category: Category, distance: Double? = null): LocationResponse {
            return LocationResponse(
                id = location.id,
                userId = location.userId,
                name = location.name,
                description = location.description,
                address = location.address,
                categoryId = location.categoryId,
                category = CategoryInfo.from(category),
                coordinates = CoordinatesResponse.from(location.coordinates),
                iconUrl = location.iconUrl,
                rating = location.rating,
                review = location.review,
                tags = location.tags,
                groupId = location.groupId,
                isActive = location.isActive,
                createdAt = location.createdAt,
                updatedAt = location.updatedAt,
                distance = distance
            )
        }
    }
}

data class CategoryInfo(
    val id: UUID,
    val code: String,
    val displayName: String,
    val icon: String?,
    val color: String?
) {
    companion object {
        fun from(category: Category): CategoryInfo {
            return CategoryInfo(
                id = category.id,
                code = category.code,
                displayName = category.displayName,
                icon = category.icon,
                color = category.color
            )
        }
    }
}

data class CoordinatesResponse(
    val latitude: BigDecimal,
    val longitude: BigDecimal
) {
    companion object {
        fun from(coordinates: Coordinates): CoordinatesResponse {
            return CoordinatesResponse(
                latitude = coordinates.latitude,
                longitude = coordinates.longitude
            )
        }
    }
}

data class CategoryStatsResponse(
    val categoryId: UUID,
    val category: CategoryInfo,
    val count: Long
) {
    companion object {
        fun from(category: Category, count: Long): CategoryStatsResponse {
            return CategoryStatsResponse(
                categoryId = category.id,
                category = CategoryInfo.from(category),
                count = count
            )
        }
    }
}
