-- ============================================================
-- V2__create_member_history_table.sql
-- 회원 이력 테이블 생성 (로그인 이력 + 정보 변경 이력 통합)
-- ============================================================

CREATE TABLE member_history (
                                id              BIGSERIAL PRIMARY KEY,
                                member_id       BIGINT          NOT NULL,                       -- member.id 참조
                                type            VARCHAR(20)     NOT NULL,                       -- LOGIN, MODIFY
                                success         BOOLEAN,                                         -- LOGIN 전용: 성공 여부 (MODIFY는 NULL)
                                field_name      VARCHAR(50),                                     -- MODIFY 전용: 변경된 필드명 (password, email 등)
                                description     VARCHAR(255),                                    -- 상세 설명 (예: "로그인 실패 - 비밀번호 불일치", "비밀번호 변경")
                                ip_address      VARCHAR(45),                                     -- 요청 IP (IPv6 고려 45자)
                                created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_member_history_member FOREIGN KEY (member_id) REFERENCES member (id)
);

COMMENT ON TABLE member_history IS '회원 이력 (로그인/정보변경 통합, type 컬럼으로 구분)';
COMMENT ON COLUMN member_history.type IS '이력 유형 (LOGIN, MODIFY)';
COMMENT ON COLUMN member_history.success IS 'LOGIN 유형 전용 - 로그인 성공 여부';
COMMENT ON COLUMN member_history.field_name IS 'MODIFY 유형 전용 - 변경된 필드명';
COMMENT ON COLUMN member_history.description IS '이력 상세 설명';
COMMENT ON COLUMN member_history.ip_address IS '요청 IP 주소';

CREATE INDEX idx_member_history_member_id ON member_history (member_id);
CREATE INDEX idx_member_history_type ON member_history (type);
CREATE INDEX idx_member_history_created_at ON member_history (created_at);