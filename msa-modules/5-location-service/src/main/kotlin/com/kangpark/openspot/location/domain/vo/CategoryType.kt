package com.kangpark.openspot.location.domain.vo

enum class CategoryType(
    val displayName: String,
    val description: String
) {
    RESTAURANT("음식점", "식당, 레스토랑, 요리점 등"),
    CAFE("카페", "커피숍, 디저트 카페, 베이커리 등"),
    SHOPPING("쇼핑", "마트, 백화점, 쇼핑몰, 전문매장 등"),
    PARK("공원", "도시공원, 놀이터, 산책로 등"),
    ENTERTAINMENT("놀거리", "영화관, 노래방, 게임장, 클럽 등"),
    ACCOMMODATION("숙소", "호텔, 모텔, 게스트하우스, 펜션 등"),
    HOSPITAL("병원", "종합병원, 의원, 치과, 약국 등"),
    EDUCATION("교육", "학교, 학원, 도서관, 미술관 등"),
    BEAUTY("뷰티", "미용실, 네일샵, 피부관리실, 마사지샵 등"),
    FITNESS("운동", "헬스장, 수영장, 요가원, 스포츠센터 등");

    companion object {
        fun fromDisplayName(displayName: String): CategoryType? {
            return values().find { it.displayName == displayName }
        }
    }
}