-- ============================================================
-- V4__add_common_code.sql
-- 공통코드 테이블 생성 (그룹+코드 분리형)
-- ============================================================

-- 코드 그룹 테이블
-- group_code가 PK. 예: POST_CATEGORY, MEMBER_STATUS
CREATE TABLE code_group (
                            group_code  VARCHAR(50)     NOT NULL,
                            label       VARCHAR(100)    NOT NULL,
                            description VARCHAR(255),
                            created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT pk_code_group PRIMARY KEY (group_code)
);

COMMENT ON TABLE code_group IS '공통코드 그룹';
COMMENT ON COLUMN code_group.group_code IS '그룹 코드 (예: POST_CATEGORY)';
COMMENT ON COLUMN code_group.label IS '그룹 이름 (예: 게시글 구분)';


-- 코드 테이블
-- (group_code, code) 복합 PK. 같은 코드값이 다른 그룹에 중복 존재 가능.
CREATE TABLE code (
                      group_code  VARCHAR(50)     NOT NULL,
                      code        VARCHAR(50)     NOT NULL,
                      label       VARCHAR(100)    NOT NULL,
                      sort_order  SMALLINT        NOT NULL DEFAULT 0,
                      enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
                      created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      CONSTRAINT pk_code PRIMARY KEY (group_code, code),
                      CONSTRAINT fk_code_group FOREIGN KEY (group_code) REFERENCES code_group (group_code)
);

COMMENT ON TABLE code IS '공통코드';
COMMENT ON COLUMN code.group_code IS '소속 그룹 코드';
COMMENT ON COLUMN code.code IS '코드값 (예: NOTICE, FREE)';
COMMENT ON COLUMN code.label IS '화면 표시 이름 (예: 공지, 일반)';
COMMENT ON COLUMN code.sort_order IS '정렬 순서 (낮을수록 위)';
COMMENT ON COLUMN code.enabled IS '활성 여부 (false면 화면에 노출 안 됨)';

CREATE INDEX idx_code_group_code ON code (group_code);


-- ===== 초기 데이터 =====

-- 게시글 구분 그룹
INSERT INTO code_group (group_code, label, description)
VALUES ('POST_CATEGORY', '게시글 구분', '게시판 게시글의 구분 코드');

-- 게시글 구분 코드
INSERT INTO code (group_code, code, label, sort_order)
VALUES
    ('POST_CATEGORY', 'NOTICE', '공지', 1),
    ('POST_CATEGORY', 'FREE',   '일반', 2);