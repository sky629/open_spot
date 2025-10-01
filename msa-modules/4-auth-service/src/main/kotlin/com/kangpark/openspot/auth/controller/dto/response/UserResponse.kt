package com.kangpark.openspot.auth.controller.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.kangpark.openspot.auth.domain.entity.User
import java.time.LocalDateTime
import java.util.UUID

/**
 * 사용자 응답 DTO
 */
data class UserResponse(
    val id: UUID,
    val socialId: String,
    val email: String,
    val name: String,
    val pictureUrl: String?,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                socialId = user.socialId,
                email = user.email,
                name = user.name,
                pictureUrl = user.pictureUrl,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}