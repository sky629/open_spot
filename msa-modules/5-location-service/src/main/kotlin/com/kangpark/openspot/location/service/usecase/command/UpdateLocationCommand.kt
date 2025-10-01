package com.kangpark.openspot.location.service.usecase.command

import java.util.*

/**
 * 장소 기본 정보 수정 Command
 */
data class UpdateLocationCommand(
    val name: String,
    val description: String?,
    val address: String?,
    val categoryId: UUID,
    val iconUrl: String? = null
)
