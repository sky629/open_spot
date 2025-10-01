package com.kangpark.openspot.location.service.usecase.command

/**
 * 장소 개인 평가 수정 Command
 */
data class UpdateLocationEvaluationCommand(
    val personalRating: Int?,
    val personalReview: String?,
    val tags: List<String>
)
