-- V1: 개인 장소 기록 서비스 초기 Schema
-- PostgreSQL 15.4 + PostGIS 지원

-- Extension 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- Schema 생성
CREATE SCHEMA IF NOT EXISTS location;

-- 1. Categories 테이블 생성 (카테고리 마스터)
CREATE TABLE location.categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    color VARCHAR(7),  -- #RRGGBB 형식
    "order" INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_category_code ON location.categories(code);
CREATE INDEX idx_category_active ON location.categories(is_active);

COMMENT ON TABLE location.categories IS '장소 카테고리 마스터';
COMMENT ON COLUMN location.categories.code IS '카테고리 코드 (RESTAURANT, CAFE 등)';
COMMENT ON COLUMN location.categories.display_name IS '표시명 (음식점, 카페 등)';

-- 기본 카테고리 데이터 INSERT
INSERT INTO location.categories (code, display_name, description, icon, color, "order") VALUES
('RESTAURANT', '음식점', '식당, 레스토랑, 요리점 등', 'restaurant', '#FF5722', 1),
('CAFE', '카페', '커피숍, 디저트 카페, 베이커리 등', 'local_cafe', '#795548', 2),
('SHOPPING', '쇼핑', '마트, 백화점, 쇼핑몰, 전문매장 등', 'shopping_cart', '#E91E63', 3),
('PARK', '공원', '도시공원, 놀이터, 산책로 등', 'park', '#4CAF50', 4),
('ENTERTAINMENT', '놀거리', '영화관, 노래방, 게임장, 클럽 등', 'movie', '#9C27B0', 5),
('ACCOMMODATION', '숙소', '호텔, 모텔, 게스트하우스, 펜션 등', 'hotel', '#2196F3', 6),
('HOSPITAL', '병원', '종합병원, 의원, 치과, 약국 등', 'local_hospital', '#F44336', 7),
('EDUCATION', '교육', '학교, 학원, 도서관, 미술관 등', 'school', '#3F51B5', 8),
('BEAUTY', '뷰티', '미용실, 네일샵, 피부관리실, 마사지샵 등', 'face', '#E91E63', 9),
('FITNESS', '운동', '헬스장, 수영장, 요가원, 스포츠센터 등', 'fitness_center', '#FF9800', 10);

-- 2. LocationGroup 테이블 생성
CREATE TABLE location.location_groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    color VARCHAR(7),  -- #RRGGBB 형식
    icon VARCHAR(50),
    "order" INTEGER NOT NULL DEFAULT 0,
    is_shared BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_location_group_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_location_group_user_id ON location.location_groups(user_id);
CREATE INDEX idx_location_group_user_order ON location.location_groups(user_id, "order");

COMMENT ON TABLE location.location_groups IS '사용자의 장소 그룹 (맛집, 카페 등)';
COMMENT ON COLUMN location.location_groups.user_id IS '그룹 소유자 ID';
COMMENT ON COLUMN location.location_groups.name IS '그룹명';
COMMENT ON COLUMN location.location_groups."order" IS '표시 순서';

-- 3. Locations 테이블 생성 (개인 장소 기록)
CREATE TABLE location.locations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    address VARCHAR(200),
    category_id UUID,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    coordinates GEOMETRY(Point, 4326) NOT NULL,
    icon_url VARCHAR(500),

    -- 개인 평가 정보
    personal_rating INTEGER CHECK (personal_rating >= 1 AND personal_rating <= 5),
    personal_review TEXT,
    tags TEXT[],

    -- 그룹 관리
    group_id UUID,

    -- 즐겨찾기
    is_favorite BOOLEAN NOT NULL DEFAULT false,

    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_location_category FOREIGN KEY (category_id)
        REFERENCES location.categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_location_group FOREIGN KEY (group_id)
        REFERENCES location.location_groups(id) ON DELETE SET NULL
);

-- Locations 인덱스
CREATE INDEX idx_location_user_id ON location.locations(user_id);
CREATE INDEX idx_location_category_id ON location.locations(category_id);
CREATE INDEX idx_location_group_id ON location.locations(group_id);
CREATE INDEX idx_location_coordinates ON location.locations USING GIST(coordinates);
CREATE INDEX idx_location_user_category ON location.locations(user_id, category_id);
CREATE INDEX idx_location_user_group ON location.locations(user_id, group_id);
CREATE INDEX idx_location_user_favorite ON location.locations(user_id, is_favorite);
CREATE INDEX idx_location_created_at ON location.locations(created_at DESC);

COMMENT ON TABLE location.locations IS '개인 장소 기록';
COMMENT ON COLUMN location.locations.user_id IS '장소 소유자 ID';
COMMENT ON COLUMN location.locations.category_id IS '카테고리 ID (FK)';
COMMENT ON COLUMN location.locations.personal_rating IS '개인 평점 (1-5)';
COMMENT ON COLUMN location.locations.personal_review IS '개인 리뷰/메모';
COMMENT ON COLUMN location.locations.tags IS '개인 태그 배열';
COMMENT ON COLUMN location.locations.group_id IS '속한 그룹 ID';
COMMENT ON COLUMN location.locations.is_favorite IS '즐겨찾기 여부';

-- 4. 좌표 자동 생성 트리거
CREATE OR REPLACE FUNCTION location.update_coordinates()
RETURNS TRIGGER AS $$
BEGIN
    NEW.coordinates = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_coordinates
    BEFORE INSERT OR UPDATE OF latitude, longitude ON location.locations
    FOR EACH ROW
    EXECUTE FUNCTION location.update_coordinates();

-- 5. updated_at 자동 갱신 트리거
CREATE OR REPLACE FUNCTION location.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_categories_updated_at
    BEFORE UPDATE ON location.categories
    FOR EACH ROW
    EXECUTE FUNCTION location.update_updated_at();

CREATE TRIGGER trigger_locations_updated_at
    BEFORE UPDATE ON location.locations
    FOR EACH ROW
    EXECUTE FUNCTION location.update_updated_at();

CREATE TRIGGER trigger_location_groups_updated_at
    BEFORE UPDATE ON location.location_groups
    FOR EACH ROW
    EXECUTE FUNCTION location.update_updated_at();