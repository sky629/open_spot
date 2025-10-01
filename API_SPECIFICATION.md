# Open-Spot API Specification

**Version**: 1.0.0
**Base URL**: `http://localhost:8080` (Gateway)
**API Version**: v1

## Overview

Open-Spot은 사용자가 방문한 장소를 기록하고 개인 평가를 관리할 수 있는 위치 기반 서비스입니다.

### Architecture

- **Gateway Service** (Port 8080): 모든 API 요청의 진입점
- **Auth Service** (Port 8081): Google OAuth2 인증 및 사용자 관리
- **Location Service** (Port 8082): 장소 기록 및 그룹 관리
- **Notification Service** (Port 8083): 푸시 알림 관리

### Authentication

대부분의 API는 JWT Bearer 토큰 인증이 필요합니다.

```http
Authorization: Bearer {access_token}
```

### Common Response Format

모든 API 응답은 다음 공통 포맷을 사용합니다:

**성공 응답**:
```json
{
  "success": true,
  "data": { /* response data */ },
  "error": null
}
```

**실패 응답**:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지"
  }
}
```

### Pagination

페이지네이션을 지원하는 API는 다음 query parameters를 사용합니다:
- `page`: 페이지 번호 (0부터 시작, 기본값: 0)
- `size`: 페이지 크기 (기본값: 20)

**페이지네이션 응답 형식**:
```json
{
  "content": [ /* 데이터 배열 */ ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

---

## 1. Auth Service APIs

Base Path: `/api/v1`

### 1.1 Google OAuth2 로그인 시작

**Endpoint**: `GET /auth/google/login`
**Description**: Google OAuth2 로그인 플로우를 시작합니다. 브라우저에서 직접 접근하면 Google 로그인 페이지로 리다이렉트됩니다.
**Authentication**: 불필요

**Response**: Redirect to Google OAuth2 authorization URL

**OAuth2 콜백 후 프론트엔드로 전달되는 정보**:
```json
{
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "socialId": "google_123456789",
    "email": "user@example.com",
    "name": "홍길동",
    "pictureUrl": "https://example.com/profile.jpg",
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00"
  },
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440001",
  "accessTokenExpiresIn": 86400000,
  "isNewUser": true
}
```

---

### 1.2 JWT 토큰 갱신

**Endpoint**: `POST /auth/token/refresh`
**Description**: Refresh 토큰을 사용하여 새로운 Access 토큰을 발급받습니다.
**Authentication**: 불필요

**Request Body**:
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440001"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440002",
    "accessTokenExpiresIn": 86400000
  },
  "error": null
}
```

**Error Response** (401 Unauthorized):
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "TOKEN_REFRESH_FAILED",
    "message": "유효하지 않거나 만료된 리프레시 토큰입니다"
  }
}
```

---

### 1.3 로그아웃

**Endpoint**: `POST /auth/logout`
**Description**: 현재 사용자의 Refresh 토큰을 무효화하여 로그아웃합니다.
**Authentication**: Required (Bearer Token)

**Headers**:
```
Authorization: Bearer {access_token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "Successfully logged out"
  },
  "error": null
}
```

---

### 1.4 현재 사용자 프로필 조회

**Endpoint**: `GET /users/self`
**Description**: JWT 토큰을 통해 현재 로그인한 사용자의 프로필 정보를 조회합니다.
**Authentication**: Required (Bearer Token)

**Headers**:
```
Authorization: Bearer {access_token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "socialId": "google_123456789",
    "email": "user@example.com",
    "name": "홍길동",
    "pictureUrl": "https://example.com/profile.jpg",
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00"
  },
  "error": null
}
```

---

### 1.5 사용자 프로필 조회 (ID 기반)

**Endpoint**: `GET /users/{userId}`
**Description**: 특정 사용자 ID로 프로필 정보를 조회합니다. (향후 친구 기능용)
**Authentication**: Required (Bearer Token)

**Path Parameters**:
- `userId` (UUID, required): 조회할 사용자 ID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "socialId": "google_123456789",
    "email": "user@example.com",
    "name": "홍길동",
    "pictureUrl": "https://example.com/profile.jpg",
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00"
  },
  "error": null
}
```

---

## 2. Location Service APIs

Base Path: `/api/v1`

**Note**: 모든 Location Service API는 `X-User-Id` 헤더가 필요합니다. (Gateway에서 JWT 토큰을 파싱하여 자동 추가)

### 2.1 카테고리 목록 조회

**Endpoint**: `GET /categories`
**Description**: 사용 가능한 모든 장소 카테고리 목록을 조회합니다.
**Authentication**: Public (인증 불필요)

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "cat-001",
      "code": "RESTAURANT",
      "displayName": "음식점",
      "description": "맛집 및 일반 식당",
      "icon": "restaurant",
      "color": "#FF5722",
      "order": 1
    },
    {
      "id": "cat-002",
      "code": "CAFE",
      "displayName": "카페",
      "description": "커피숍 및 카페",
      "icon": "local_cafe",
      "color": "#795548",
      "order": 2
    },
    {
      "id": "cat-003",
      "code": "SHOPPING",
      "displayName": "쇼핑",
      "description": "쇼핑몰, 편의점, 마트 등",
      "icon": "shopping_bag",
      "color": "#9C27B0",
      "order": 3
    }
  ],
  "error": null
}
```

---

### 2.2 새 장소 생성

**Endpoint**: `POST /locations`
**Description**: 새로운 개인 장소를 등록합니다.
**Authentication**: Required

**Headers**:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Request Body**:
```json
{
  "name": "맛있는 파스타집",
  "description": "친구들과 자주 가는 이탈리안 레스토랑",
  "address": "서울특별시 강남구 테헤란로 123",
  "categoryId": "cat-001",
  "latitude": 37.5012345,
  "longitude": 127.0398765,
  "iconUrl": "https://example.com/icon.png",
  "personalRating": 5,
  "personalReview": "파스타가 정말 맛있고 분위기도 좋아요",
  "tags": ["파스타", "데이트", "분위기좋음"],
  "groupId": "group-001"
}
```

**Validation Rules**:
- `name`: 필수, 최대 100자
- `description`: 선택, 최대 1000자
- `address`: 선택, 최대 200자
- `categoryId`: 필수, UUID
- `latitude`: 필수, -90.0 ~ 90.0
- `longitude`: 필수, -180.0 ~ 180.0
- `personalRating`: 선택, 1 ~ 5
- `personalReview`: 선택, 최대 2000자
- `tags`: 선택, 최대 10개, 각 태그 최대 20자
- `groupId`: 선택, UUID (null이면 그룹 미지정)

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": "loc-001",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "맛있는 파스타집",
    "description": "친구들과 자주 가는 이탈리안 레스토랑",
    "address": "서울특별시 강남구 테헤란로 123",
    "categoryId": "cat-001",
    "category": {
      "id": "cat-001",
      "code": "RESTAURANT",
      "displayName": "음식점",
      "icon": "restaurant",
      "color": "#FF5722"
    },
    "coordinates": {
      "latitude": 37.5012345,
      "longitude": 127.0398765
    },
    "iconUrl": "https://example.com/icon.png",
    "personalRating": 5,
    "personalReview": "파스타가 정말 맛있고 분위기도 좋아요",
    "tags": ["파스타", "데이트", "분위기좋음"],
    "groupId": "group-001",
    "isActive": true,
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00",
    "distance": null
  },
  "error": null
}
```

---

### 2.3 장소 상세 조회

**Endpoint**: `GET /locations/{locationId}`
**Description**: 특정 장소의 상세 정보를 조회합니다. 본인의 장소만 조회 가능합니다.
**Authentication**: Required

**Path Parameters**:
- `locationId` (UUID, required): 장소 ID

**Headers**:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Response** (200 OK): 동일한 LocationResponse 형식

---

### 2.4 장소 기본 정보 수정

**Endpoint**: `PUT /locations/{locationId}`
**Description**: 장소의 기본 정보 (이름, 설명, 주소, 카테고리)를 수정합니다.
**Authentication**: Required

**Request Body**:
```json
{
  "name": "더 맛있는 파스타집",
  "description": "리뉴얼 후 더 좋아진 곳",
  "address": "서울특별시 강남구 테헤란로 456",
  "categoryId": "cat-001",
  "iconUrl": "https://example.com/new-icon.png"
}
```

---

### 2.5 개인 평가 정보 수정

**Endpoint**: `PUT /locations/{locationId}/evaluation`
**Description**: 장소에 대한 개인 평점, 리뷰, 태그를 수정합니다.
**Authentication**: Required

**Request Body**:
```json
{
  "personalRating": 4,
  "personalReview": "여전히 맛있지만 가격이 조금 올랐어요",
  "tags": ["파스타", "데이트", "가격상승"]
}
```

---

### 2.6 장소 그룹 변경

**Endpoint**: `PUT /locations/{locationId}/group`
**Description**: 장소가 속한 그룹을 변경합니다.
**Authentication**: Required

**Request Body**:
```json
{
  "groupId": "group-002"
}
```

---

### 2.7 장소 좌표 수정

**Endpoint**: `PUT /locations/{locationId}/coordinates`
**Description**: 장소의 GPS 좌표를 수정합니다.
**Authentication**: Required

**Request Body**:
```json
{
  "latitude": 37.5098765,
  "longitude": 127.0412345
}
```

---

### 2.8 장소 비활성화 (삭제)

**Endpoint**: `DELETE /locations/{locationId}`
**Description**: 장소를 비활성화합니다 (논리적 삭제).
**Authentication**: Required

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "locationId": "loc-001",
    "message": "장소가 비활성화되었습니다"
  },
  "error": null
}
```

---

### 2.9 장소 목록 조회 (다양한 검색 조건)

**Endpoint**: `GET /locations`
**Description**: 다양한 조건으로 장소를 검색합니다.
**Authentication**: Required

**Query Parameters**:

**지도 영역 검색 (우선순위 1)**:
- `northEastLat` (double): 북동쪽 위도
- `northEastLon` (double): 북동쪽 경도
- `southWestLat` (double): 남서쪽 위도
- `southWestLon` (double): 남서쪽 경도

**반경 검색 (우선순위 2)**:
- `latitude` (double): 중심점 위도
- `longitude` (double): 중심점 경도
- `radiusMeters` (double): 반경 (100~50000)

**필터**:
- `categoryId` (UUID): 카테고리 필터
- `keyword` (string): 키워드 검색
- `targetUserId` (UUID): 조회할 사용자

**페이지네이션**:
- `page` (int, default: 0)
- `size` (int, default: 20)

**Example - 지도 영역 내 검색**:
```
GET /locations?northEastLat=37.52&northEastLon=127.05&southWestLat=37.48&southWestLon=127.02
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "loc-001",
        "name": "맛있는 파스타집",
        "categoryId": "cat-001",
        "category": {
          "id": "cat-001",
          "code": "RESTAURANT",
          "displayName": "음식점",
          "icon": "restaurant",
          "color": "#FF5722"
        },
        "coordinates": {
          "latitude": 37.5012345,
          "longitude": 127.0398765
        },
        "personalRating": 5,
        "tags": ["파스타", "데이트"],
        "distance": 523.45
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  },
  "error": null
}
```

---

### 2.10 최고 평점 장소 목록

**Endpoint**: `GET /locations/top-rated`
**Description**: 내 개인 평점 기준 최고 평점 장소 목록 (5점 만점 순)
**Authentication**: Required

---

### 2.11 최근 등록 장소 목록

**Endpoint**: `GET /locations/recent`
**Description**: 최근 등록한 내 장소 목록 (최신순)
**Authentication**: Required

---

### 2.12 내 장소 목록

**Endpoint**: `GET /locations/self`
**Description**: 내가 생성한 모든 장소 목록
**Authentication**: Required

---

### 2.13 그룹별 장소 목록

**Endpoint**: `GET /locations/groups/{groupId}`
**Description**: 특정 그룹에 속한 내 장소 목록
**Authentication**: Required

---

## 3. Location Group APIs

Base Path: `/api/v1/locations/groups`

### 3.1 그룹 생성

**Endpoint**: `POST /locations/groups`
**Description**: 새로운 장소 그룹을 생성합니다.
**Authentication**: Required

**Request Body**:
```json
{
  "name": "맛집 투어",
  "description": "가고 싶은 맛집 리스트",
  "color": "#FF5722",
  "icon": "restaurant"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": "group-001",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "맛집 투어",
    "description": "가고 싶은 맛집 리스트",
    "color": "#FF5722",
    "icon": "restaurant",
    "order": 0,
    "isShared": false,
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00"
  },
  "error": null
}
```

---

### 3.2 그룹 목록 조회

**Endpoint**: `GET /locations/groups`
**Description**: 사용자의 모든 그룹 목록 (order 순서대로)
**Authentication**: Required

---

### 3.3 그룹 수정

**Endpoint**: `PUT /locations/groups/{groupId}`
**Description**: 그룹의 기본 정보 수정
**Authentication**: Required

---

### 3.4 그룹 순서 변경

**Endpoint**: `PUT /locations/groups/reorder`
**Description**: 드래그 앤 드롭으로 그룹 순서 변경
**Authentication**: Required

**Request Body**:
```json
{
  "groupOrders": [
    { "groupId": "group-002", "order": 0 },
    { "groupId": "group-001", "order": 1 }
  ]
}
```

---

### 3.5 그룹 삭제

**Endpoint**: `DELETE /locations/groups/{groupId}`
**Description**: 그룹 삭제 (속한 장소들은 그룹 미지정 상태로)
**Authentication**: Required

---

## 4. Notification Service APIs

Base Path: `/api/v1/notifications`

### 4.1 디바이스 토큰 등록

**Endpoint**: `POST /notifications/tokens`
**Description**: FCM 푸시 알림을 위한 디바이스 토큰 등록
**Authentication**: Required (JWT)

**Request Body**:
```json
{
  "token": "fcm_device_token_here...",
  "deviceType": "WEB",
  "deviceId": "device_unique_id"
}
```

**deviceType**: `WEB`, `ANDROID`, `IOS` 중 하나

---

### 4.2 알림 목록 조회

**Endpoint**: `GET /notifications`
**Description**: 사용자의 알림 목록을 페이지네이션으로 조회
**Authentication**: Required (JWT)

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "noti-001",
        "title": "분석 리포트가 완성되었습니다",
        "body": "강남의 상권 분석 리포트가 완성되었습니다.",
        "notificationType": "REPORT_COMPLETE",
        "referenceId": "report-001",
        "isRead": false,
        "isSent": true,
        "createdAt": "2025-01-01T10:00:00",
        "readAt": null
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1,
      "first": true,
      "last": true
    }
  },
  "error": null
}
```

---

### 4.3 읽지 않은 알림 수 조회

**Endpoint**: `GET /notifications/unread-count`
**Description**: 읽지 않은 알림 개수 조회
**Authentication**: Required (JWT)

**Response**:
```json
{
  "success": true,
  "data": {
    "unreadCount": 5
  },
  "error": null
}
```

---

### 4.4 알림 읽음 처리

**Endpoint**: `PUT /notifications/{notificationId}/read`
**Description**: 특정 알림을 읽음 상태로 변경
**Authentication**: Required (JWT)

---

### 4.5 알림 설정 조회

**Endpoint**: `GET /notifications/settings`
**Description**: 사용자의 알림 설정 조회
**Authentication**: Required (JWT)

**Response**:
```json
{
  "success": true,
  "data": {
    "reportEnabled": true,
    "systemEnabled": true
  },
  "error": null
}
```

---

### 4.6 알림 설정 업데이트

**Endpoint**: `PUT /notifications/settings`
**Description**: 사용자의 알림 설정 업데이트
**Authentication**: Required (JWT)

**Request Body**:
```json
{
  "reportEnabled": true,
  "systemEnabled": false
}
```

---

## 5. Frontend Implementation Guide

### 5.1 인증 플로우

```javascript
// 1. Google OAuth2 로그인
window.location.href = 'http://localhost:8080/api/v1/auth/google/login';

// 2. 콜백 후 토큰 저장
const { accessToken, refreshToken } = parseCallback();
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);

// 3. API 요청 시 토큰 추가
const response = await fetch('http://localhost:8080/api/v1/users/self', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});
```

---

### 5.2 지도 검색 구현

**지도 영역 내 장소 검색**:
```javascript
const bounds = map.getBounds();
const ne = bounds.getNorthEast();
const sw = bounds.getSouthWest();

const response = await fetch(
  `http://localhost:8080/api/v1/locations?` +
  `northEastLat=${ne.lat}&northEastLon=${ne.lng}&` +
  `southWestLat=${sw.lat}&southWestLon=${sw.lng}`,
  {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  }
);
```

---

## 6. Development & Testing

### Swagger UI

- Auth Service: http://localhost:8081/swagger-ui.html
- Location Service: http://localhost:8082/swagger-ui.html
- Notification Service: http://localhost:8083/swagger-ui.html

### Health Check

```bash
curl http://localhost:8081/api/v1/auth/health
curl http://localhost:8082/api/v1/locations/health
curl http://localhost:8083/api/v1/notifications/health
```

---

**문서 작성 완료**

이 API 문서는 OpenAPI 3.0 스펙에 기반하여 작성되었으며, 프론트엔드 개발팀이 즉시 구현을 시작할 수 있도록 상세한 요청/응답 예시를 포함하고 있습니다.
