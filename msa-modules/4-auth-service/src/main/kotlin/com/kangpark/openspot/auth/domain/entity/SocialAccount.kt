package com.kangpark.openspot.auth.domain.entity

import com.kangpark.openspot.auth.domain.vo.SocialProvider
import com.kangpark.openspot.common.core.domain.BaseEntity
import java.time.LocalDateTime
import java.util.UUID

/**
 * 소셜 계정 도메인 엔터티
 * OAuth2 제공자를 통한 소셜 로그인 연결 정보 관리
 */
data class SocialAccount(
    val userId: UUID,
    val provider: SocialProvider,
    val providerId: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val connectedAt: LocalDateTime,

    // BaseEntity 합성
    val baseEntity: BaseEntity = BaseEntity()
) {

    // 편의 프로퍼티 (BaseEntity 필드 접근)
    val id: UUID get() = baseEntity.id
    val createdAt: LocalDateTime get() = baseEntity.createdAt
    val updatedAt: LocalDateTime get() = baseEntity.updatedAt
    
    companion object {
        /**
         * 새로운 소셜 계정 생성
         */
        fun create(
            userId: UUID,
            provider: SocialProvider,
            providerId: String,
            email: String,
            displayName: String,
            profileImageUrl: String? = null,
            connectedAt: LocalDateTime = LocalDateTime.now()
        ): SocialAccount {
            require(providerId.isNotBlank()) { "Provider ID는 필수입니다" }
            require(email.isNotBlank()) { "이메일은 필수입니다" }
            require(displayName.isNotBlank()) { "표시 이름은 필수입니다" }
            require(isValidEmail(email)) { "유효한 이메일 형식이 아닙니다" }
            
            return SocialAccount(
                userId = userId,
                provider = provider,
                providerId = providerId,
                email = email,
                displayName = displayName,
                profileImageUrl = profileImageUrl,
                connectedAt = connectedAt
            )
        }
        
        /**
         * 이메일 유효성 검증
         */
        private fun isValidEmail(email: String): Boolean {
            return email.contains("@") && email.contains(".")
        }
    }
    
    /**
     * 소셜 계정 정보 업데이트
     */
    fun updateInfo(
        email: String,
        displayName: String,
        profileImageUrl: String? = null
    ): SocialAccount {
        require(email.isNotBlank()) { "이메일은 필수입니다" }
        require(displayName.isNotBlank()) { "표시 이름은 필수입니다" }
        require(isValidEmail(email)) { "유효한 이메일 형식이 아닙니다" }

        return copy(
            email = email,
            displayName = displayName,
            profileImageUrl = profileImageUrl ?: this.profileImageUrl,
            baseEntity = baseEntity.copy(updatedAt = LocalDateTime.now())
        )
    }

    /**
     * 동일한 소셜 계정인지 확인
     */
    fun isSameAccount(provider: SocialProvider, providerId: String): Boolean {
        return this.provider == provider && this.providerId == providerId
    }
}