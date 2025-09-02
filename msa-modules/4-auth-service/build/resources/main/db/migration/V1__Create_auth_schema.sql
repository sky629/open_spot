-- Auth Service 스키마 및 테이블 생성
-- PostgreSQL 15.4, Flyway 10.0.0 호환

-- auth 스키마 생성
CREATE SCHEMA IF NOT EXISTS auth;

-- users 테이블 생성
CREATE TABLE auth.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    social_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    picture_url VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- users 테이블 인덱스
CREATE INDEX idx_users_social_id ON auth.users(social_id);
CREATE INDEX idx_users_email ON auth.users(email);

-- social_accounts 테이블 생성
CREATE TABLE auth.social_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(500),
    connected_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_social_accounts_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES auth.users(id) 
        ON DELETE CASCADE,
    
    -- 한 사용자는 동일 제공자에 하나의 계정만 연결 가능
    CONSTRAINT uk_social_accounts_user_provider 
        UNIQUE (user_id, provider),
    
    -- 동일 제공자의 동일 계정은 한 번만 연결 가능
    CONSTRAINT uk_social_accounts_provider_id 
        UNIQUE (provider, provider_id)
);

-- social_accounts 테이블 인덱스
CREATE INDEX idx_social_accounts_user_id ON auth.social_accounts(user_id);
CREATE INDEX idx_social_accounts_provider ON auth.social_accounts(provider);
CREATE INDEX idx_social_accounts_provider_id ON auth.social_accounts(provider, provider_id);
CREATE INDEX idx_social_accounts_email ON auth.social_accounts(email);
CREATE INDEX idx_social_accounts_connected_at ON auth.social_accounts(connected_at);

-- updated_at 자동 업데이트 함수 생성
CREATE OR REPLACE FUNCTION auth.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- users 테이블에 updated_at 자동 업데이트 트리거 추가
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON auth.users 
    FOR EACH ROW 
    EXECUTE FUNCTION auth.update_updated_at_column();

-- social_accounts 테이블에 updated_at 자동 업데이트 트리거 추가
CREATE TRIGGER update_social_accounts_updated_at 
    BEFORE UPDATE ON auth.social_accounts 
    FOR EACH ROW 
    EXECUTE FUNCTION auth.update_updated_at_column();

-- 테이블에 대한 코멘트 추가
COMMENT ON TABLE auth.users IS '사용자 정보 테이블 - Social OAuth2 인증';
COMMENT ON TABLE auth.social_accounts IS '소셜 계정 연결 정보 테이블 - OAuth2 제공자별 연결 관리';

-- 컬럼에 대한 코멘트 추가
COMMENT ON COLUMN auth.users.social_id IS 'Social OAuth2 사용자 ID (Google sub claim 등)';
COMMENT ON COLUMN auth.users.email IS '사용자 이메일';
COMMENT ON COLUMN auth.users.name IS '사용자 이름';
COMMENT ON COLUMN auth.users.picture_url IS '사용자 프로필 이미지 URL';

COMMENT ON COLUMN auth.social_accounts.user_id IS '연결된 사용자 ID (users 테이블 참조)';
COMMENT ON COLUMN auth.social_accounts.provider IS '소셜 로그인 제공자 (GOOGLE, FACEBOOK, NAVER, KAKAO)';
COMMENT ON COLUMN auth.social_accounts.provider_id IS '제공자별 사용자 고유 ID (예: Google sub, Facebook id)';
COMMENT ON COLUMN auth.social_accounts.email IS '소셜 계정 이메일';
COMMENT ON COLUMN auth.social_accounts.display_name IS '소셜 계정 표시 이름';
COMMENT ON COLUMN auth.social_accounts.profile_image_url IS '소셜 계정 프로필 이미지 URL';
COMMENT ON COLUMN auth.social_accounts.connected_at IS '소셜 계정 연결 시각';