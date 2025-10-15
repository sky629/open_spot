-- Notification Service 스키마 및 테이블 생성
-- PostgreSQL 15.4, Flyway 10.0.0 호환

-- notification 스키마 생성
CREATE SCHEMA IF NOT EXISTS notification;

-- device_tokens 테이블 생성 (FCM 토큰 관리)
CREATE TABLE notification.device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,  -- auth.users(id) 참조 (FK 없음 - 느슨한 결합)
    token VARCHAR(500) NOT NULL UNIQUE,
    device_type VARCHAR(20) NOT NULL CHECK (device_type IN ('WEB', 'ANDROID', 'IOS')),
    device_id VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_used_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- device_tokens 테이블 인덱스
CREATE INDEX idx_device_tokens_user_id ON notification.device_tokens(user_id);
CREATE INDEX idx_device_tokens_token ON notification.device_tokens(token);
CREATE INDEX idx_device_tokens_active ON notification.device_tokens(user_id, is_active);
CREATE INDEX idx_device_tokens_device_type ON notification.device_tokens(device_type);

-- notifications 테이블 생성 (알림 이력)
CREATE TABLE notification.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL CHECK (notification_type IN ('REPORT_COMPLETE', 'SYSTEM_NOTICE')),
    reference_id UUID,  -- 관련 리소스 ID (report_id 등)
    fcm_message_id VARCHAR(255),  -- FCM 응답 메시지 ID
    sent_at TIMESTAMP WITHOUT TIME ZONE,
    read_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- notifications 테이블 인덱스
CREATE INDEX idx_notifications_user_id ON notification.notifications(user_id);
CREATE INDEX idx_notifications_type ON notification.notifications(notification_type);
CREATE INDEX idx_notifications_user_created ON notification.notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notification.notifications(user_id, read_at) WHERE read_at IS NULL;
CREATE INDEX idx_notifications_reference ON notification.notifications(reference_id);

-- notification_settings 테이블 생성 (사용자별 알림 설정)
CREATE TABLE notification.notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    report_enabled BOOLEAN NOT NULL DEFAULT true,
    system_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- notification_settings 테이블 인덱스
CREATE INDEX idx_notification_settings_user_id ON notification.notification_settings(user_id);

-- updated_at 자동 업데이트 함수 생성
CREATE OR REPLACE FUNCTION notification.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- device_tokens 테이블에 updated_at 자동 업데이트 트리거 추가
CREATE TRIGGER update_device_tokens_updated_at
    BEFORE UPDATE ON notification.device_tokens
    FOR EACH ROW
    EXECUTE FUNCTION notification.update_updated_at_column();

-- notification_settings 테이블에 updated_at 자동 업데이트 트리거 추가
CREATE TRIGGER update_notification_settings_updated_at
    BEFORE UPDATE ON notification.notification_settings
    FOR EACH ROW
    EXECUTE FUNCTION notification.update_updated_at_column();

-- 테이블에 대한 코멘트 추가
COMMENT ON TABLE notification.device_tokens IS 'FCM 디바이스 토큰 관리 테이블';
COMMENT ON TABLE notification.notifications IS '알림 이력 테이블';
COMMENT ON TABLE notification.notification_settings IS '사용자별 알림 설정 테이블';

-- 컬럼에 대한 코멘트 추가
COMMENT ON COLUMN notification.device_tokens.user_id IS '사용자 ID (auth.users 참조, FK 없음)';
COMMENT ON COLUMN notification.device_tokens.token IS 'FCM 등록 토큰';
COMMENT ON COLUMN notification.device_tokens.device_type IS '디바이스 타입 (WEB, ANDROID, IOS)';
COMMENT ON COLUMN notification.device_tokens.device_id IS '디바이스 고유 식별자';
COMMENT ON COLUMN notification.device_tokens.is_active IS '토큰 활성화 상태';
COMMENT ON COLUMN notification.device_tokens.last_used_at IS '마지막 사용 시각';

COMMENT ON COLUMN notification.notifications.notification_type IS '알림 타입 (REPORT_COMPLETE, SYSTEM_NOTICE)';
COMMENT ON COLUMN notification.notifications.reference_id IS '관련 리소스 ID (report_id 등)';
COMMENT ON COLUMN notification.notifications.fcm_message_id IS 'FCM 메시지 ID';
COMMENT ON COLUMN notification.notifications.sent_at IS '알림 전송 시각';
COMMENT ON COLUMN notification.notifications.read_at IS '알림 읽은 시각';

COMMENT ON COLUMN notification.notification_settings.report_enabled IS '분석 리포트 완료 알림 활성화';
COMMENT ON COLUMN notification.notification_settings.system_enabled IS '시스템 공지 알림 활성화';