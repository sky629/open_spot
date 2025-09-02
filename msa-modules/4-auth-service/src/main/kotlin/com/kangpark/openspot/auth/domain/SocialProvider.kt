package com.kangpark.openspot.auth.domain

/**
 * 소셜 로그인 제공자 열거형
 * 지원하는 OAuth2 제공자 목록
 */
enum class SocialProvider(
    val displayName: String,
    val baseUrl: String
) {
    GOOGLE("Google", "https://accounts.google.com"),
    FACEBOOK("Facebook", "https://www.facebook.com"),
    NAVER("Naver", "https://nid.naver.com"),
    KAKAO("Kakao", "https://kauth.kakao.com");
    
    companion object {
        /**
         * 문자열로부터 SocialProvider 찾기
         */
        fun fromString(provider: String): SocialProvider {
            return values().find { it.name.equals(provider, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unsupported social provider: $provider")
        }
    }
}