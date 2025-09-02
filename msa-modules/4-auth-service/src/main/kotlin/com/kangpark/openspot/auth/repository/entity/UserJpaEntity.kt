package com.kangpark.openspot.auth.repository.entity

import com.kangpark.openspot.auth.domain.User
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
 * User JPA 엔터티
 * Domain 모델과 데이터베이스 테이블 간의 매핑
 */
@Entity
@Table(
    name = "users", 
    schema = "auth",
    indexes = [
        Index(name = "idx_users_social_id", columnList = "social_id"),
        Index(name = "idx_users_email", columnList = "email")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class UserJpaEntity(

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "social_id", nullable = false, unique = true)
    val socialId: String,

    @Column(name = "email", nullable = false)
    val email: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "picture_url")
    val pictureUrl: String? = null,

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
    fun toDomain(): User {
        val user = User.create(
            socialId = socialId,
            email = email,
            name = name,
            pictureUrl = pictureUrl
        )
        // BaseEntity의 id, createdAt, updatedAt은 reflection을 통해 설정
        setBaseEntityFields(user)
        return user
    }
    
    /**
     * BaseEntity 필드 설정
     */
    private fun setBaseEntityFields(user: User) {
        // Domain 엔터티의 BaseEntity 필드들을 JPA 엔터티 값으로 설정
        val idField = User::class.java.superclass.getDeclaredField("id")
        val createdAtField = User::class.java.superclass.getDeclaredField("createdAt")
        val updatedAtField = User::class.java.superclass.getDeclaredField("updatedAt")
        
        idField.isAccessible = true
        createdAtField.isAccessible = true
        updatedAtField.isAccessible = true
        
        idField.set(user, this.id)
        createdAtField.set(user, this.createdAt)
        updatedAtField.set(user, this.updatedAt)
    }
    
    companion object {
        /**
         * Domain 모델에서 JPA 엔터티로 변환
         */
        fun fromDomain(user: User): UserJpaEntity {
            return UserJpaEntity(
                socialId = user.socialId,
                email = user.email,
                name = user.name,
                pictureUrl = user.pictureUrl
            )
        }
    }
}