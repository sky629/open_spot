package com.kangpark.openspot.location.controller.dto.request

import com.kangpark.openspot.location.domain.vo.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.util.*

// Enums
enum class LocationSortBy {
    RATING,        // 평점 높은 순
    CREATED_AT     // 최근 등록순 (기본값)
}

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

    @field:DecimalMin(value = "0.5", message = "평점은 0.5 이상이어야 합니다")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0 이하이어야 합니다")
    val rating: Double? = null,

    @field:Size(max = 2000, message = "리뷰는 2000자를 초과할 수 없습니다")
    val review: String? = null,

    @field:Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다")
    val tags: List<@Size(max = 20, message = "각 태그는 20자를 초과할 수 없습니다") String> = emptyList(),

    val groupId: UUID? = null
) {
    fun toCoordinates(): Coordinates = Coordinates(latitude, longitude)
}

data class UpdateLocationRequest(
    // 기본 정보 (모두 optional, 제공된 필드만 업데이트)
    @field:Size(max = 100, message = "장소명은 100자를 초과할 수 없습니다")
    val name: String? = null,

    @field:Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    val description: String? = null,

    @field:Size(max = 200, message = "주소는 200자를 초과할 수 없습니다")
    val address: String? = null,

    val categoryId: UUID? = null,

    @field:Size(max = 500, message = "아이콘 URL은 500자를 초과할 수 없습니다")
    val iconUrl: String? = null,

    // 평가 정보 (모두 optional)
    @field:DecimalMin(value = "0.5", message = "평점은 0.5 이상이어야 합니다")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0 이하이어야 합니다")
    val rating: Double? = null,

    @field:Size(max = 2000, message = "리뷰는 2000자를 초과할 수 없습니다")
    val review: String? = null,

    @field:Size(max = 10, message = "태그는 최대 10개까지 등록할 수 있습니다")
    val tags: List<@Size(max = 20, message = "각 태그는 20자를 초과할 수 없습니다") String>? = null,

    // 그룹 (optional)
    val groupId: UUID? = null,

    // 좌표 (latitude와 longitude가 함께 업데이트되어야 함)
    val coordinates: CoordinatesRequest? = null
)

data class CoordinatesRequest(
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

    val groupId: UUID? = null,

    @field:Size(max = 100, message = "검색어는 100자를 초과할 수 없습니다")
    val keyword: String? = null,

    // 정렬 (기본 조회 시에만 적용, 필터가 있으면 기본 정렬 사용)
    val sortBy: LocationSortBy? = null,

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
