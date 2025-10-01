package com.kangpark.openspot.auth.domain.entity

import com.kangpark.openspot.common.core.domain.BaseEntity
import java.time.LocalDateTime
import java.util.UUID

/**
 * 사용자 도메인 엔터티
 * Social OAuth2를 통한 인증 정보를 관리
 */
data class User(
    val socialId: String,
    val email: String,
    val name: String,
    val pictureUrl: String? = null,

    // BaseEntity 합성
    val baseEntity: BaseEntity = BaseEntity()
) {

    // 편의 프로퍼티 (BaseEntity 필드 접근)
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt
    
    companion object {
        fun create(
            socialId: String,
            email: String,
            name: String,
            pictureUrl: String? = null
        ): User {
            require(socialId.isNotBlank()) { "Social ID는 필수입니다" }
            require(email.isNotBlank()) { "이메일은 필수입니다" }
            require(name.isNotBlank()) { "이름은 필수입니다" }
            require(isValidEmail(email)) { "유효한 이메일 형식이 아닙니다" }
            
            return User(
                socialId = socialId,
                email = email,
                name = name,
                pictureUrl = pictureUrl
            )
        }
        
        private fun isValidEmail(email: String): Boolean {
            return email.contains("@") && email.contains(".")
        }
    }
    
    fun updateProfile(name: String, pictureUrl: String?): User {
        require(name.isNotBlank()) { "이름은 필수입니다" }

        return copy(
            name = name,
            pictureUrl = pictureUrl,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }
}