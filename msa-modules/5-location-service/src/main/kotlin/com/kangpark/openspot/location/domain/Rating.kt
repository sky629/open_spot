package com.kangpark.openspot.location.domain

import jakarta.persistence.Embeddable
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal
import java.math.RoundingMode

@Embeddable
data class Rating(
    @field:DecimalMin(value = "1.0", message = "평점은 1점 이상이어야 합니다")
    @field:DecimalMax(value = "5.0", message = "평점은 5점 이하이어야 합니다")
    val score: BigDecimal
) {
    init {
        require(score.scale() <= 1) { "평점은 소수점 첫째 자리까지만 허용됩니다" }
        require(score >= BigDecimal.ONE && score <= BigDecimal.valueOf(5)) {
            "평점은 1.0 이상 5.0 이하이어야 합니다"
        }
    }

    /**
     * 별점을 정수로 변환 (반올림)
     */
    fun toStars(): Int {
        return score.setScale(0, RoundingMode.HALF_UP).toInt()
    }

    /**
     * 평점을 문자열로 표현
     */
    fun toDisplayString(): String {
        return "★".repeat(toStars()) + "☆".repeat(5 - toStars()) + " (${score})"
    }

    companion object {
        fun of(score: Double): Rating {
            return Rating(BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP))
        }

        fun of(score: Int): Rating {
            require(score in 1..5) { "평점은 1 이상 5 이하의 정수여야 합니다" }
            return Rating(BigDecimal.valueOf(score.toLong()))
        }

        /**
         * 여러 평점의 평균을 계산
         */
        fun average(ratings: List<Rating>): Rating? {
            if (ratings.isEmpty()) return null

            val sum = ratings.sumOf { it.score }
            val average = sum.divide(BigDecimal.valueOf(ratings.size.toLong()), 1, RoundingMode.HALF_UP)

            return Rating(average)
        }
    }
}