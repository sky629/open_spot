package com.kangpark.openspot.location.controller

import com.kangpark.openspot.common.web.dto.ApiResponse
import com.kangpark.openspot.location.controller.dto.response.*
import com.kangpark.openspot.location.service.usecase.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "LocationGroup", description = "위치 그룹 관리 API")
@RestController
@RequestMapping("/api/v1/locations/groups")
class LocationGroupController(
    private val createLocationGroupUseCase: CreateLocationGroupUseCase,
    private val getLocationGroupsUseCase: GetLocationGroupsUseCase,
    private val updateLocationGroupUseCase: UpdateLocationGroupUseCase,
    private val reorderLocationGroupsUseCase: ReorderLocationGroupsUseCase,
    private val deleteLocationGroupUseCase: DeleteLocationGroupUseCase
) {
    private val logger = LoggerFactory.getLogger(LocationGroupController::class.java)

    @Operation(summary = "그룹 생성", description = "사용자의 새로운 장소 그룹을 생성합니다.")
    @PostMapping
    fun createGroup(
        @Valid @RequestBody request: CreateLocationGroupRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<LocationGroupResponse>> {
        return try {
            val locationGroup = createLocationGroupUseCase.execute(
                userId = userId,
                name = request.name,
                description = request.description,
                color = request.color,
                icon = request.icon
            )

            val response = LocationGroupResponse.from(locationGroup)
            ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid group creation request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to create location group", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "그룹 목록 조회", description = "사용자의 모든 그룹 목록을 조회합니다 (order 순서대로).")
    @GetMapping
    fun getGroups(
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<List<LocationGroupResponse>>> {
        return try {
            val groups = getLocationGroupsUseCase.execute(userId)
            val responses = groups.map { LocationGroupResponse.from(it) }
            ResponseEntity.ok(ApiResponse.success(responses))

        } catch (e: Exception) {
            logger.error("Failed to get location groups", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "그룹 수정", description = "그룹의 기본 정보를 수정합니다.")
    @PutMapping("/{groupId}")
    fun updateGroup(
        @Parameter(description = "그룹 ID", required = true)
        @PathVariable groupId: UUID,
        @Valid @RequestBody request: UpdateLocationGroupRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<LocationGroupResponse>> {
        return try {
            val locationGroup = updateLocationGroupUseCase.execute(
                groupId = groupId,
                userId = userId,
                name = request.name,
                description = request.description,
                color = request.color,
                icon = request.icon
            )

            val response = LocationGroupResponse.from(locationGroup)
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid group update request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to update location group: {}", groupId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "그룹 순서 변경", description = "드래그 앤 드롭으로 그룹 순서를 변경합니다.")
    @PutMapping("/reorder")
    fun reorderGroups(
        @Valid @RequestBody request: ReorderLocationGroupsRequest,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<List<LocationGroupResponse>>> {
        return try {
            val groupIdOrders = request.groupOrders.associate { it.groupId to it.order }
            val reorderedGroups = reorderLocationGroupsUseCase.execute(userId, groupIdOrders)
            val responses = reorderedGroups.map { LocationGroupResponse.from(it) }
            ResponseEntity.ok(ApiResponse.success(responses))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid group reorder request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to reorder location groups", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }

    @Operation(summary = "그룹 삭제", description = "그룹을 삭제합니다. 그룹에 속한 장소들은 그룹에서 제외됩니다.")
    @DeleteMapping("/{groupId}")
    fun deleteGroup(
        @Parameter(description = "그룹 ID", required = true)
        @PathVariable groupId: UUID,
        @Parameter(description = "사용자 ID", required = true)
        @RequestHeader("X-User-Id") userId: UUID
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return try {
            deleteLocationGroupUseCase.execute(groupId, userId)

            val response = mapOf(
                "groupId" to groupId,
                "message" to "그룹이 삭제되었습니다"
            )
            ResponseEntity.ok(ApiResponse.success(response))

        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid group deletion request: {}", e.message)
            ResponseEntity.badRequest().body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = e.message ?: "잘못된 요청입니다"
                    )
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to delete location group: {}", groupId, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                    com.kangpark.openspot.common.web.dto.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다"
                    )
                )
            )
        }
    }
}