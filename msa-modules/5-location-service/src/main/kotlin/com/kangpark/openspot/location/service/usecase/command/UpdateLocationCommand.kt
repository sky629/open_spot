package com.kangpark.openspot.location.service.usecase.command

import com.kangpark.openspot.location.domain.vo.Coordinates
import java.util.*

/**
 * 장소 통합 수정 Command (부분 업데이트)
 * 제공된 필드만 업데이트됨
 */
data class UpdateLocationCommand(
    // 기본 정보 (모두 optional)
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val categoryId: UUID? = null,
    val iconUrl: String? = null,

    // 평가 정보 (모두 optional)
    val rating: Double? = null,
    val review: String? = null,
    val tags: List<String>? = null,

    // 그룹 (optional)
    val groupId: UUID? = null,

    // 좌표 (optional, 함께 업데이트됨)
    val coordinates: Coordinates? = null
)
