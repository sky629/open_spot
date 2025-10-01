package com.kangpark.openspot.location.controller.dto.response

import com.kangpark.openspot.location.domain.entity.LocationGroup
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import java.util.*

// Request DTOs
data class CreateLocationGroupRequest(
    @field:NotBlank(message = "그룹명은 필수입니다")
    @field:Size(max = 100, message = "그룹명은 100자를 초과할 수 없습니다")
    val name: String,

    @field:Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    val description: String? = null,

    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다")
    val color: String? = null,

    @field:Size(max = 50, message = "아이콘명은 50자를 초과할 수 없습니다")
    val icon: String? = null
)

data class UpdateLocationGroupRequest(
    @field:NotBlank(message = "그룹명은 필수입니다")
    @field:Size(max = 100, message = "그룹명은 100자를 초과할 수 없습니다")
    val name: String,

    @field:Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    val description: String? = null,

    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다")
    val color: String? = null,

    @field:Size(max = 50, message = "아이콘명은 50자를 초과할 수 없습니다")
    val icon: String? = null
)

data class ReorderLocationGroupsRequest(
    @field:NotEmpty(message = "그룹 순서 정보는 필수입니다")
    val groupOrders: List<GroupOrderItem>
)

data class GroupOrderItem(
    @field:NotNull(message = "그룹 ID는 필수입니다")
    val groupId: UUID,

    @field:Min(value = 0, message = "순서는 0 이상이어야 합니다")
    val order: Int
)

// Response DTOs
data class LocationGroupResponse(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val description: String?,
    val color: String?,
    val icon: String?,
    val order: Int,
    val isShared: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(locationGroup: LocationGroup): LocationGroupResponse {
            return LocationGroupResponse(
                id = locationGroup.id,
                userId = locationGroup.userId,
                name = locationGroup.name,
                description = locationGroup.description,
                color = locationGroup.color,
                icon = locationGroup.icon,
                order = locationGroup.order,
                isShared = locationGroup.isShared,
                createdAt = locationGroup.createdAt,
                updatedAt = locationGroup.updatedAt
            )
        }
    }
}

data class LocationGroupSummaryResponse(
    val id: UUID,
    val name: String,
    val color: String?,
    val icon: String?,
    val order: Int,
    val locationCount: Long = 0
) {
    companion object {
        fun from(locationGroup: LocationGroup, locationCount: Long = 0): LocationGroupSummaryResponse {
            return LocationGroupSummaryResponse(
                id = locationGroup.id,
                name = locationGroup.name,
                color = locationGroup.color,
                icon = locationGroup.icon,
                order = locationGroup.order,
                locationCount = locationCount
            )
        }
    }
}