package com.kangpark.openspot.location.domain.valueobject

import jakarta.persistence.Embeddable
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal

@Embeddable
data class Coordinates(
    @field:DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
    @field:DecimalMax(value = "90.0", message = "위도는 90도 이하이어야 합니다")
    val latitude: BigDecimal,

    @field:DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
    @field:DecimalMax(value = "180.0", message = "경도는 180도 이하이어야 합니다")
    val longitude: BigDecimal
) {
    init {
        require(latitude.scale() <= 8) { "위도 소수점은 8자리를 초과할 수 없습니다" }
        require(longitude.scale() <= 8) { "경도 소수점은 8자리를 초과할 수 없습니다" }
    }

    /**
     * 두 좌표 간의 거리를 미터 단위로 계산 (Haversine formula)
     */
    fun distanceTo(other: Coordinates): Double {
        val earthRadius = 6371000.0 // 지구 반지름 (미터)

        val lat1Rad = Math.toRadians(this.latitude.toDouble())
        val lat2Rad = Math.toRadians(other.latitude.toDouble())
        val deltaLatRad = Math.toRadians((other.latitude - this.latitude).toDouble())
        val deltaLngRad = Math.toRadians((other.longitude - this.longitude).toDouble())

        val a = kotlin.math.sin(deltaLatRad / 2) * kotlin.math.sin(deltaLatRad / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(deltaLngRad / 2) * kotlin.math.sin(deltaLngRad / 2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }

    companion object {
        fun of(latitude: Double, longitude: Double): Coordinates {
            return Coordinates(
                latitude = BigDecimal.valueOf(latitude),
                longitude = BigDecimal.valueOf(longitude)
            )
        }
    }
}