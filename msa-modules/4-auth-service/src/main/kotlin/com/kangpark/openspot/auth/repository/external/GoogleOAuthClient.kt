package com.kangpark.openspot.auth.repository.external

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * Google OAuth2 클라이언트
 * Circuit Breaker 패턴을 적용하여 Google API 호출 시 장애 격리
 */
@Component
class GoogleOAuthClient {
    
    /**
     * Google OAuth2 사용자 정보
     */
    data class GoogleUserInfo(
        val socialId: String,
        val email: String,
        val name: String,
        val pictureUrl: String?,
        val emailVerified: Boolean = true
    ) {
        companion object {
            fun from(oauth2User: OAuth2User): GoogleUserInfo {
                return GoogleUserInfo(
                    socialId = oauth2User.getAttribute<String>("sub")
                        ?: throw IllegalArgumentException("Google sub claim is missing"),
                    email = oauth2User.getAttribute<String>("email") 
                        ?: throw IllegalArgumentException("Google email claim is missing"),
                    name = oauth2User.getAttribute<String>("name") 
                        ?: oauth2User.getAttribute<String>("given_name") 
                        ?: "Unknown User",
                    pictureUrl = oauth2User.getAttribute<String>("picture"),
                    emailVerified = oauth2User.getAttribute<Boolean>("email_verified") ?: false
                )
            }
        }
    }
    
    /**
     * OAuth2User에서 Google 사용자 정보 추출
     * Circuit Breaker, Retry, TimeLimiter 적용
     */
    @CircuitBreaker(name = "google-oauth", fallbackMethod = "fallbackExtractUserInfo")
    @Retry(name = "google-oauth")
    @TimeLimiter(name = "google-oauth")
    fun extractUserInfo(oauth2User: OAuth2User): CompletableFuture<GoogleUserInfo> {
        return CompletableFuture.supplyAsync {
            try {
                val userInfo = GoogleUserInfo.from(oauth2User)
                validateUserInfo(userInfo)
                userInfo
            } catch (e: Exception) {
                throw GoogleOAuthException("Failed to extract Google user info: ${e.message}", e)
            }
        }
    }
    
    /**
     * 동기적 사용자 정보 추출 (기존 호환성)
     */
    @CircuitBreaker(name = "google-oauth", fallbackMethod = "fallbackExtractUserInfoSync")
    @Retry(name = "google-oauth")
    fun extractUserInfoSync(oauth2User: OAuth2User): GoogleUserInfo {
        try {
            val userInfo = GoogleUserInfo.from(oauth2User)
            validateUserInfo(userInfo)
            return userInfo
        } catch (e: Exception) {
            throw GoogleOAuthException("Failed to extract Google user info: ${e.message}", e)
        }
    }
    
    /**
     * 사용자 정보 유효성 검증
     */
    private fun validateUserInfo(userInfo: GoogleUserInfo) {
        require(userInfo.socialId.isNotBlank()) { "Social ID is required" }
        require(userInfo.email.isNotBlank()) { "Email is required" }
        require(userInfo.name.isNotBlank()) { "Name is required" }
        require(userInfo.emailVerified) { "Email must be verified by Google" }
        require(isValidEmail(userInfo.email)) { "Invalid email format: ${userInfo.email}" }
    }
    
    /**
     * 이메일 형식 검증
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length > 5
    }
    
    /**
     * Circuit Breaker Fallback - 비동기
     */
    private fun fallbackExtractUserInfo(
        oauth2User: OAuth2User, 
        exception: Exception
    ): CompletableFuture<GoogleUserInfo> {
        return CompletableFuture.failedFuture(
            GoogleOAuthException("Google OAuth service is temporarily unavailable. Please try again later.", exception)
        )
    }
    
    /**
     * Circuit Breaker Fallback - 동기
     */
    private fun fallbackExtractUserInfoSync(
        oauth2User: OAuth2User, 
        exception: Exception
    ): GoogleUserInfo {
        throw GoogleOAuthException("Google OAuth service is temporarily unavailable. Please try again later.", exception)
    }
    
    /**
     * Google OAuth 관련 예외
     */
    class GoogleOAuthException(
        message: String, 
        cause: Throwable? = null
    ) : RuntimeException(message, cause)
}