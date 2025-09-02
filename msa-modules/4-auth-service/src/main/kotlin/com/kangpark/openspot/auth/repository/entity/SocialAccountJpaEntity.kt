package com.kangpark.openspot.auth.repository.entity

import com.kangpark.openspot.auth.domain.SocialAccount
import com.kangpark.openspot.auth.domain.SocialProvider
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

/**
 * SocialAccount JPA 엔터티
 * Domain 모델과 데이터베이스 테이블 간의 매핑
 */
@Entity
@Table(
    name = "social_accounts", 
    schema = "auth",
    indexes = [
        Index(name = "idx_social_accounts_user_id", columnList = "user_id"),
        Index(name = "idx_social_accounts_provider", columnList = "provider"),
        Index(name = "idx_social_accounts_provider_id", columnList = "provider, provider_id"),
        Index(name = "idx_social_accounts_email", columnList = "email"),
        Index(name = "idx_social_accounts_connected_at", columnList = "connected_at")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_social_accounts_user_provider", columnNames = ["user_id", "provider"]),
        UniqueConstraint(name = "uk_social_accounts_provider_id", columnNames = ["provider", "provider_id"])
    ]
)
@EntityListeners(AuditingEntityListener::class)
class SocialAccountJpaEntity(

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID? = null,

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    val provider: SocialProvider,

    @Column(name = "provider_id", nullable = false)
    val providerId: String,

    @Column(name = "email", nullable = false)
    val email: String,

    @Column(name = "display_name", nullable = false)
    val displayName: String,

    @Column(name = "profile_image_url")
    val profileImageUrl: String? = null,

    @Column(name = "connected_at", nullable = false)
    val connectedAt: LocalDateTime,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    /**
     * Domain 모델로 변환
     */
    fun toDomain(): SocialAccount {
        val socialAccount = SocialAccount.create(
            userId = userId,
            provider = provider,
            providerId = providerId,
            email = email,
            displayName = displayName,
            profileImageUrl = profileImageUrl,
            connectedAt = connectedAt
        )
        // BaseEntity의 id, createdAt, updatedAt은 reflection을 통해 설정
        setBaseEntityFields(socialAccount)
        return socialAccount
    }
    
    /**
     * BaseEntity 필드 설정
     */
    private fun setBaseEntityFields(socialAccount: SocialAccount) {
        // Domain 엔터티의 BaseEntity 필드들을 JPA 엔터티 값으로 설정
        val idField = SocialAccount::class.java.superclass.getDeclaredField("id")
        val createdAtField = SocialAccount::class.java.superclass.getDeclaredField("createdAt")
        val updatedAtField = SocialAccount::class.java.superclass.getDeclaredField("updatedAt")
        
        idField.isAccessible = true
        createdAtField.isAccessible = true
        updatedAtField.isAccessible = true
        
        idField.set(socialAccount, this.id)
        createdAtField.set(socialAccount, this.createdAt)
        updatedAtField.set(socialAccount, this.updatedAt)
    }
    
    companion object {
        /**
         * Domain 모델에서 JPA 엔터티로 변환
         */
        fun fromDomain(socialAccount: SocialAccount): SocialAccountJpaEntity {
            return SocialAccountJpaEntity(
                userId = socialAccount.userId,
                provider = socialAccount.provider,
                providerId = socialAccount.providerId,
                email = socialAccount.email,
                displayName = socialAccount.displayName,
                profileImageUrl = socialAccount.profileImageUrl,
                connectedAt = socialAccount.connectedAt
            )
        }
    }
}