package com.kangpark.openspot.location.controller.dto.response

import com.kangpark.openspot.location.domain.entity.*
import com.kangpark.openspot.location.domain.vo.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
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
    val categoryId: UUID,

    @field:NotNull(message = "위도는 필수입니다")
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다")
    val latitude: BigDecimal,

    @field:NotNull(message = "경도는 필수입니다")
    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다")
    val longitude: BigDecimal,

    @field:Size(max = 500, message = "아이콘 URL은 500자를 초과할 수 없습니다")
    val iconUrl: String? = null,

    @field:Min(value = 1, message = "개인 평점은 1 이상이어야 합니다")
    @field:Max(value = 5, message = "개인 평점은 5 이하이어야 합니다")
    val personalRating: Int? = null,

    @field:Size(max = 2000, message = "개인 리뷰는 2000자를 초과할 수 없습니다")
    val personalReview: String? = null,

    @field:Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다")
    val tags: List<@Size(max = 20, message = "각 태그는 20자를 초과할 수 없습니다") String> = emptyList(),

    val groupId: UUID? = null
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
    val categoryId: UUID,

    @field:Size(max = 500, message = "아이콘 URL은 500자를 초과할 수 없습니다")
    val iconUrl: String? = null
)

data class UpdateLocationEvaluationRequest(
    @field:Min(value = 1, message = "개인 평점은 1 이상이어야 합니다")
    @field:Max(value = 5, message = "개인 평점은 5 이하이어야 합니다")
    val personalRating: Int? = null,

    @field:Size(max = 2000, message = "개인 리뷰는 2000자를 초과할 수 없습니다")
    val personalReview: String? = null,

    @field:Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다")
    val tags: List<@Size(max = 20, message = "각 태그는 20자를 초과할 수 없습니다") String> = emptyList()
)

data class ChangeLocationGroupRequest(
    val groupId: UUID? = null
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
    // 반경 검색 (중심점 + 반경)
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다")
    val latitude: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다")
    val longitude: Double? = null,

    @field:Min(value = 100, message = "검색 반경은 최소 100m 이상이어야 합니다")
    @field:Max(value = 50000, message = "검색 반경은 최대 50km 이하이어야 합니다")
    val radiusMeters: Double? = null,

    // 지도 영역 검색 (bounds)
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다")
    val northEastLat: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다")
    val northEastLon: Double? = null,

    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다")
    val southWestLat: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다")
    val southWestLon: Double? = null,

    // 필터
    val categoryId: UUID? = null,

    @field:Size(max = 100, message = "검색어는 100자를 초과할 수 없습니다")
    val keyword: String? = null,

    // 타겟 유저 (향후 친구 기능용)
    val targetUserId: UUID? = null
) {
    fun hasBounds(): Boolean {
        return northEastLat != null && northEastLon != null &&
               southWestLat != null && southWestLon != null
    }

    fun hasRadius(): Boolean {
        return latitude != null && longitude != null && radiusMeters != null
    }
}

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
    val personalRating: Int?,
    val personalReview: String?,
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
                personalRating = location.personalRating,
                personalReview = location.personalReview,
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

data class LocationSummaryResponse(
    val id: UUID,
    val name: String,
    val categoryId: UUID,
    val category: CategoryInfo,
    val coordinates: CoordinatesResponse,
    val personalRating: Int?,
    val tags: List<String>,
    val distance: Double? = null
) {
    companion object {
        fun from(location: Location, category: Category, distance: Double? = null): LocationSummaryResponse {
            return LocationSummaryResponse(
                id = location.id,
                name = location.name,
                categoryId = location.categoryId,
                category = CategoryInfo.from(category),
                coordinates = CoordinatesResponse.from(location.coordinates),
                personalRating = location.personalRating,
                tags = location.tags,
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