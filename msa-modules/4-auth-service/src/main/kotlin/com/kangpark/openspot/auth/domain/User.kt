package com.kangpark.openspot.auth.domain

import com.kangpark.openspot.common.core.domain.BaseEntity
import java.util.UUID

/**
 * 사용자 도메인 엔터티
 * Social OAuth2를 통한 인증 정보를 관리
 */
class User(
    val socialId: String,
    val email: String,
    val name: String,
    val pictureUrl: String? = null
) : BaseEntity() {
    
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
        
        return User(
            socialId = this.socialId,
            email = this.email,
            name = name,
            pictureUrl = pictureUrl
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return socialId == other.socialId
    }
    
    override fun hashCode(): Int {
        return socialId.hashCode()
    }
    
    override fun toString(): String {
        return "User(socialId='$socialId', email='$email', name='$name')"
    }
}