package com.kangpark.openspot.location.domain.vo

/**
 * 방문 목적 값 객체
 * 사용자가 장소를 방문하는 목적을 나타냄
 */
enum class VisitPurpose(val displayName: String) {
    BUSINESS("업무"),
    LEISURE("여가"),
    DATING("데이트"),
    FAMILY("가족모임"),
    FRIENDS("친구모임"),
    SOLO("혼자"),
    EXERCISE("운동"),
    STUDY("공부"),
    SHOPPING("쇼핑"),
    DINING("식사"),
    OTHER("기타")
}