-- ============================================================
-- V3__add_oauth_support.sql
-- 소셜 로그인(OAuth2) 지원을 위한 스키마 변경
-- ============================================================

-- 구글 등 소셜 로그인 전용 가입자는 아이디/비밀번호 자체가 없으므로 nullable로 변경.
-- (PostgreSQL은 NULL끼리 서로 다른 값으로 취급하므로, uk_member_username 제약과 충돌하지 않음 -
--  즉 username이 NULL인 행이 여러 개 있어도 UNIQUE 제약 위반이 아님)
ALTER TABLE member ALTER COLUMN username DROP NOT NULL;
ALTER TABLE member ALTER COLUMN password DROP NOT NULL;

COMMENT ON COLUMN member.username IS '로그인 아이디 (평문). 소셜 로그인 전용 가입자는 NULL';
COMMENT ON COLUMN member.password IS 'Bcrypt 단방향 암호화 비밀번호. 소셜 로그인 전용 가입자는 NULL';


-- 소셜 로그인 연동 정보.
-- member와 별도 테이블로 분리해, 기존 회원가입/로그인 로직(member 테이블)을 거의 건드리지 않고
-- 소셜 로그인을 추가할 수 있도록 한다. 한 회원이 여러 provider를 연동하는 것도 자연스럽게 표현 가능.
CREATE TABLE member_oauth (
                              id          BIGSERIAL PRIMARY KEY,
                              member_id   BIGINT          NOT NULL,
                              provider    VARCHAR(20)     NOT NULL,                   -- GOOGLE 등
                              provider_id VARCHAR(255)    NOT NULL,                    -- 소셜 서비스가 발급하는 사용자 고유 식별자 (구글: sub)
                              created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_member_oauth_member FOREIGN KEY (member_id) REFERENCES member (id),
                              CONSTRAINT uk_member_oauth_provider UNIQUE (provider, provider_id)
);

COMMENT ON TABLE member_oauth IS '회원의 소셜 로그인(OAuth2) 연동 정보';
COMMENT ON COLUMN member_oauth.provider IS '소셜 로그인 제공자 (GOOGLE 등)';
COMMENT ON COLUMN member_oauth.provider_id IS '제공자가 발급하는 사용자 고유 식별자';

CREATE INDEX idx_member_oauth_member_id ON member_oauth (member_id);