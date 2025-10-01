package com.kangpark.openspot.auth.domain.repository

import com.kangpark.openspot.auth.domain.entity.User
import java.util.UUID

/**
 * 사용자 리포지토리 인터페이스
 * Domain Layer에서 정의하여 Infrastructure Layer에서 구현
 */
interface UserRepository {
    
    /**
     * 사용자 저장
     */
    fun save(user: User): User
    
    /**
     * ID로 사용자 조회
     */
    fun findById(id: UUID): User?
    
    /**
     * Social ID로 사용자 조회
     */
    fun findBySocialId(socialId: String): User?
    
    /**
     * 이메일로 사용자 조회
     */
    fun findByEmail(email: String): User?
    
    /**
     * 사용자 존재 여부 확인
     */
    fun existsBySocialId(socialId: String): Boolean
    
    /**
     * 사용자 삭제
     */
    fun deleteById(id: UUID)
}