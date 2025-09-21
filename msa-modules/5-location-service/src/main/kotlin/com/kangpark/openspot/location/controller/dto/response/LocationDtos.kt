package com.kangpark.openspot.location.controller.dto.response

import com.kangpark.openspot.location.domain.entity.*
import com.kangpark.openspot.location.domain.valueobject.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

// Request DTOs
data class CreateLocationRequest(
    @field:NotBlank(message = "장소명은 필수입니다")
    @field:Size(max = 100, message = "장소명은 100자를 초과할 수 없습니다")
    val name: String,

    @field:Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    val description: String? = null,

    @field:Size(max = 200, message = "주소는 200자를 초과할 수 없습니다")
    val address: String? = null,

    @field:NotNull(message = "카테고리는 필수입니다")
    val category: CategoryType,

    @field:NotNull(message = "위도는 필수입니다")
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다")
    val latitude: BigDecimal,

    @field:NotNull(message = "경도는 필수입니다")
    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다")
    val longitude: BigDecimal,

    @field:Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다")
    val phoneNumber: String? = null,

    @field:Size(max = 500, message = "웹사이트 URL은 500자를 초과할 수 없습니다")
    val websiteUrl: String? = null,

    @field:Size(max = 500, message = "영업시간은 500자를 초과할 수 없습니다")
    val businessHours: String? = null
) {
    fun toCoordinates(): Coordinates = Coordinates(latitude, longitude)
}

data class UpdateLocationRequest(
    @field:NotBlank(message = "장소명은 필수입니다")
    @field:Size(max = 100, message = "장소명은 100자를 초과할 수 없습니다")
    val name: String,

    @field:Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    val description: String? = null,

    @field:Size(max = 200, message = "주소는 200자를 초과할 수 없습니다")
    val address: String? = null,

    @field:NotNull(message = "카테고리는 필수입니다")
    val category: CategoryType,

    @field:Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다")
    val phoneNumber: String? = null,

    @field:Size(max = 500, message = "웹사이트 URL은 500자를 초과할 수 없습니다")
    val websiteUrl: String? = null,

    @field:Size(max = 500, message = "영업시간은 500자를 초과할 수 없습니다")
    val businessHours: String? = null
)

data class UpdateLocationCoordinatesRequest(
    @field:NotNull(message = "위도는 필수입니다")
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다")
    val latitude: BigDecimal,

    @field:NotNull(message = "경도는 필수입니다")
    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다")
    val longitude: BigDecimal
) {
    fun toCoordinates(): Coordinates = Coordinates(latitude, longitude)
}

data class LocationSearchRequest(
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다")
    val latitude: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다")
    val longitude: Double? = null,

    @field:Min(value = 100, message = "검색 반경은 최소 100m 이상이어야 합니다")
    @field:Max(value = 50000, message = "검색 반경은 최대 50km 이하이어야 합니다")
    val radiusMeters: Double? = null,

    val category: CategoryType? = null,

    @field:Size(max = 100, message = "검색어는 100자를 초과할 수 없습니다")
    val keyword: String? = null
)

// Response DTOs
data class LocationResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val address: String?,
    val category: CategoryType,
    val categoryDisplayName: String,
    val coordinates: CoordinatesResponse,
    val createdBy: UUID,
    val phoneNumber: String?,
    val websiteUrl: String?,
    val businessHours: String?,
    val isActive: Boolean,
    val viewCount: Long,
    val averageRating: BigDecimal?,
    val reviewCount: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val distance: Double? = null
) {
    companion object {
        fun from(location: Location, distance: Double? = null): LocationResponse {
            return LocationResponse(
                id = location.id!!,
                name = location.name,
                description = location.description,
                address = location.address,
                category = location.category,
                categoryDisplayName = location.category.displayName,
                coordinates = CoordinatesResponse.from(location.coordinates),
                createdBy = location.createdBy,
                phoneNumber = location.phoneNumber,
                websiteUrl = location.websiteUrl,
                businessHours = location.businessHours,
                isActive = location.isActive,
                viewCount = location.viewCount,
                averageRating = location.averageRating,
                reviewCount = location.reviewCount,
                createdAt = location.createdAt!!,
                updatedAt = location.updatedAt!!,
                distance = distance
            )
        }
    }
}

data class LocationSummaryResponse(
    val id: UUID,
    val name: String,
    val category: CategoryType,
    val categoryDisplayName: String,
    val coordinates: CoordinatesResponse,
    val averageRating: BigDecimal?,
    val reviewCount: Long,
    val distance: Double? = null
) {
    companion object {
        fun from(location: Location, distance: Double? = null): LocationSummaryResponse {
            return LocationSummaryResponse(
                id = location.id!!,
                name = location.name,
                category = location.category,
                categoryDisplayName = location.category.displayName,
                coordinates = CoordinatesResponse.from(location.coordinates),
                averageRating = location.averageRating,
                reviewCount = location.reviewCount,
                distance = distance
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

data class LocationStatsResponse(
    val locationId: UUID,
    val viewCount: Long,
    val visitCount: Long,
    val uniqueVisitorCount: Long,
    val reviewCount: Long,
    val averageRating: BigDecimal?,
    val favoriteCount: Long
)

data class CategoryStatsResponse(
    val category: CategoryType,
    val categoryDisplayName: String,
    val count: Long
) {
    companion object {
        fun from(category: CategoryType, count: Long): CategoryStatsResponse {
            return CategoryStatsResponse(
                category = category,
                categoryDisplayName = category.displayName,
                count = count
            )
        }
    }
}