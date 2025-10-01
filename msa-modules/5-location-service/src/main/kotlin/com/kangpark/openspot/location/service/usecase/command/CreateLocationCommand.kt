package com.kangpark.openspot.location.service.usecase.command

import com.kangpark.openspot.location.domain.vo.Coordinates
import java.util.*

/**
 * 장소 생성 Command
 */
data class CreateLocationCommand(
    val name: String,
    val description: String?,
    val address: String?,
    val categoryId: UUID,
    val coordinates: Coordinates,
    val iconUrl: String? = null,
    val personalRating: Int? = null,
    val personalReview: String? = null,
    val tags: List<String> = emptyList(),
    val groupId: UUID? = null
)
