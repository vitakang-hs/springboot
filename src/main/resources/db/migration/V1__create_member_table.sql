-- ============================================================
-- V1__create_member_table.sql
-- 회원 테이블 생성
-- ============================================================

CREATE TABLE member (
                        id                      BIGSERIAL PRIMARY KEY,
                        username                VARCHAR(20)     NOT NULL,                   -- 로그인 아이디 (영문/숫자 4자 이상, 평문 저장)
                        password                VARCHAR(100)    NOT NULL,                   -- BCrypt 단방향 해시 (실제 60자 고정, 여유 100)

                        email                   VARCHAR(255)    NOT NULL,                   -- AES 양방향 암호화된 이메일 (복호화 후 화면 표시/발송용)
                        email_hash              VARCHAR(64)     NOT NULL,                   -- 이메일 SHA-256 해시 (검색/중복확인용, 결정적 해시라 동일 입력 시 동일 출력)

    -- email_verified 컬럼 없음: 회원가입/이메일 변경 모두 "인증 통과 후에만 저장"되는 흐름이라
    -- DB에 저장된 이메일은 항상 인증된 상태이므로 별도 플래그가 불필요함

                        role                    VARCHAR(20)     NOT NULL DEFAULT 'USER',    -- USER, ADMIN
                        status                  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE(정상), LOCKED(잠김), WITHDRAWN(탈퇴)

                        login_fail_count        SMALLINT        NOT NULL DEFAULT 0,        -- 로그인 연속 실패 횟수 (5회 도달 시 LOCKED 처리)

                        created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted_at              TIMESTAMP,                                  -- 탈퇴(soft delete) 처리 시각

                        CONSTRAINT uk_member_username UNIQUE (username),
                        CONSTRAINT uk_member_email_hash UNIQUE (email_hash)
);

COMMENT ON TABLE member IS '회원';
COMMENT ON COLUMN member.username IS '로그인 아이디 (평문)';
COMMENT ON COLUMN member.password IS 'BCrypt 단방향 암호화 비밀번호';
COMMENT ON COLUMN member.email IS 'AES 양방향 암호화된 이메일 원문 (표시/발송용 복호화 대상)';
COMMENT ON COLUMN member.email_hash IS '이메일 SHA-256 해시 (검색, 중복확인용 - 결정적 해시)';
COMMENT ON COLUMN member.role IS '회원 권한 (USER, ADMIN)';
COMMENT ON COLUMN member.status IS '계정 상태 (ACTIVE: 정상, LOCKED: 잠김, WITHDRAWN: 탈퇴)';
COMMENT ON COLUMN member.login_fail_count IS '로그인 연속 실패 횟수 (5회 도달 시 자동 LOCKED)';
COMMENT ON COLUMN member.deleted_at IS '탈퇴 처리 시각 (soft delete)';

CREATE INDEX idx_member_status ON member (status);