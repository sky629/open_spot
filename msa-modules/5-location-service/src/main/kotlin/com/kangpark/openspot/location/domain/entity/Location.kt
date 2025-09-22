package com.kangpark.openspot.location.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import com.kangpark.openspot.location.domain.vo.CategoryType
import com.kangpark.openspot.location.domain.vo.Coordinates
import com.kangpark.openspot.location.domain.vo.Rating
import java.math.BigDecimal
import java.util.*

/**
 * 장소 도메인 엔터티
 * 사용자가 등록하고 공유하는 장소 정보를 관리
 */
class Location(
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val category: CategoryType,
    val coordinates: Coordinates,
    val createdBy: UUID,
    val phoneNumber: String? = null,
    val websiteUrl: String? = null,
    val businessHours: String? = null,
    val isActive: Boolean = true,
    val viewCount: Long = 0L,
    val averageRating: BigDecimal? = null,
    val reviewCount: Long = 0L
) : BaseEntity() {

    /**
     * 평점 업데이트
     */
    fun updateRating(newAverageRating: BigDecimal?, newReviewCount: Long): Location {
        return Location(
            name = this.name,
            description = this.description,
            address = this.address,
            category = this.category,
            coordinates = this.coordinates,
            createdBy = this.createdBy,
            phoneNumber = this.phoneNumber,
            websiteUrl = this.websiteUrl,
            businessHours = this.businessHours,
            isActive = this.isActive,
            viewCount = this.viewCount,
            averageRating = newAverageRating,
            reviewCount = newReviewCount
        )
    }

    /**
     * 조회수 증가
     */
    fun incrementViewCount(): Location {
        return Location(
            name = this.name,
            description = this.description,
            address = this.address,
            category = this.category,
            coordinates = this.coordinates,
            createdBy = this.createdBy,
            phoneNumber = this.phoneNumber,
            websiteUrl = this.websiteUrl,
            businessHours = this.businessHours,
            isActive = this.isActive,
            viewCount = this.viewCount + 1,
            averageRating = this.averageRating,
            reviewCount = this.reviewCount
        )
    }

    /**
     * 장소 정보 업데이트
     */
    fun updateBasicInfo(
        name: String,
        description: String?,
        address: String?,
        category: CategoryType
    ): Location {
        require(name.isNotBlank()) { "장소명은 필수입니다" }
        require(name.length <= 100) { "장소명은 100자를 초과할 수 없습니다" }
        require(description?.length ?: 0 <= 1000) { "설명은 1000자를 초과할 수 없습니다" }
        require(address?.length ?: 0 <= 200) { "주소는 200자를 초과할 수 없습니다" }

        return Location(
            name = name,
            description = description,
            address = address,
            category = category,
            coordinates = this.coordinates,
            createdBy = this.createdBy,
            phoneNumber = this.phoneNumber,
            websiteUrl = this.websiteUrl,
            businessHours = this.businessHours,
            isActive = this.isActive,
            viewCount = this.viewCount,
            averageRating = this.averageRating,
            reviewCount = this.reviewCount
        )
    }

    /**
     * 연락처 정보 업데이트
     */
    fun updateContactInfo(
        phoneNumber: String?,
        websiteUrl: String?,
        businessHours: String?
    ): Location {
        require(phoneNumber?.length ?: 0 <= 20) { "전화번호는 20자를 초과할 수 없습니다" }
        require(websiteUrl?.length ?: 0 <= 500) { "웹사이트 URL은 500자를 초과할 수 없습니다" }
        require(businessHours?.length ?: 0 <= 500) { "영업시간은 500자를 초과할 수 없습니다" }

        return Location(
            name = this.name,
            description = this.description,
            address = this.address,
            category = this.category,
            coordinates = this.coordinates,
            createdBy = this.createdBy,
            phoneNumber = phoneNumber,
            websiteUrl = websiteUrl,
            businessHours = businessHours,
            isActive = this.isActive,
            viewCount = this.viewCount,
            averageRating = this.averageRating,
            reviewCount = this.reviewCount
        )
    }

    /**
     * 좌표 업데이트
     */
    fun updateCoordinates(coordinates: Coordinates): Location {
        return Location(
            name = this.name,
            description = this.description,
            address = this.address,
            category = this.category,
            coordinates = coordinates,
            createdBy = this.createdBy,
            phoneNumber = this.phoneNumber,
            websiteUrl = this.websiteUrl,
            businessHours = this.businessHours,
            isActive = this.isActive,
            viewCount = this.viewCount,
            averageRating = this.averageRating,
            reviewCount = this.reviewCount
        )
    }

    /**
     * 장소 비활성화
     */
    fun deactivate(): Location {
        return Location(
            name = this.name,
            description = this.description,
            address = this.address,
            category = this.category,
            coordinates = this.coordinates,
            createdBy = this.createdBy,
            phoneNumber = this.phoneNumber,
            websiteUrl = this.websiteUrl,
            businessHours = this.businessHours,
            isActive = false,
            viewCount = this.viewCount,
            averageRating = this.averageRating,
            reviewCount = this.reviewCount
        )
    }

    /**
     * 장소 활성화
     */
    fun activate(): Location {
        return Location(
            name = this.name,
            description = this.description,
            address = this.address,
            category = this.category,
            coordinates = this.coordinates,
            createdBy = this.createdBy,
            phoneNumber = this.phoneNumber,
            websiteUrl = this.websiteUrl,
            businessHours = this.businessHours,
            isActive = true,
            viewCount = this.viewCount,
            averageRating = this.averageRating,
            reviewCount = this.reviewCount
        )
    }

    /**
     * 평균 평점 반환 (Rating 객체로)
     */
    fun getAverageRatingAsRating(): Rating? {
        return averageRating?.let { Rating(it) }
    }

    /**
     * 특정 좌표와의 거리 계산
     */
    fun distanceTo(targetCoordinates: Coordinates): Double {
        return coordinates.distanceTo(targetCoordinates)
    }

    /**
     * 장소 소유자 확인
     */
    fun isOwnedBy(userId: UUID): Boolean {
        return createdBy == userId
    }

    companion object {
        fun create(
            name: String,
            description: String?,
            address: String?,
            category: CategoryType,
            coordinates: Coordinates,
            createdBy: UUID,
            phoneNumber: String? = null,
            websiteUrl: String? = null,
            businessHours: String? = null
        ): Location {
            require(name.isNotBlank()) { "장소명은 필수입니다" }
            require(name.length <= 100) { "장소명은 100자를 초과할 수 없습니다" }
            require(description?.length ?: 0 <= 1000) { "설명은 1000자를 초과할 수 없습니다" }
            require(address?.length ?: 0 <= 200) { "주소는 200자를 초과할 수 없습니다" }
            require(phoneNumber?.length ?: 0 <= 20) { "전화번호는 20자를 초과할 수 없습니다" }
            require(websiteUrl?.length ?: 0 <= 500) { "웹사이트 URL은 500자를 초과할 수 없습니다" }
            require(businessHours?.length ?: 0 <= 500) { "영업시간은 500자를 초과할 수 없습니다" }

            return Location(
                name = name,
                description = description,
                address = address,
                category = category,
                coordinates = coordinates,
                createdBy = createdBy,
                phoneNumber = phoneNumber,
                websiteUrl = websiteUrl,
                businessHours = businessHours
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Location) return false
        return name == other.name && coordinates == other.coordinates
    }

    override fun hashCode(): Int {
        return Objects.hash(name, coordinates)
    }

    override fun toString(): String {
        return "Location(name='$name', category=$category, coordinates=$coordinates)"
    }
}