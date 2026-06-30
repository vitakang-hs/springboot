-- ============================================================
-- V7__replace_common_code_with_system_config.sql
-- 공통코드(code_group/code) 폐기, 설정 페이지(system_config_group/item)로 전환
--
-- 배경:
--   V4에서 만든 code_group/code는 "관리자가 자유롭게 코드를 추가/관리"하는
--   유연한 구조를 의도했으나, 실제로는 코드를 추가해도 그 코드를 어디서
--   어떻게 사용할지는 결국 개발자가 직접 코드를 작성해야 하므로
--   "자유롭게 추가 가능"이라는 장점이 거의 의미가 없었다.
--
--   또한 "공지 최대 노출 개수", "페이지 크기 선택지" 같은 단일 설정값을
--   표현하기에도 부적합한 구조였다 (code 테이블은 "선택지 목록"을
--   표현하기 위한 구조이지, key-value 설정을 위한 구조가 아님).
--
--   이에 따라 공통코드 테이블을 폐기하고, 관리자 전용 "설정 페이지"
--   개념으로 단순화한다. 게시글 카테고리(NOTICE, FREE 등)는 더 이상
--   DB가 아닌 애플리케이션 enum 상수로 관리한다.
--
-- 신설:
--   system_config_group : 설정 페이지의 좌측 메뉴(타이틀) 단위. 예: 로그인, 게시판
--   system_config_item   : 그룹에 속한 개별 설정 항목(key-value). 예: 로그인 실패 허용 횟수 = 5
-- ============================================================

DROP TABLE IF EXISTS code;
DROP TABLE IF EXISTS code_group;


-- 설정 그룹 (설정 페이지 좌측 메뉴 단위)
CREATE TABLE system_config_group (
                                     group_code  VARCHAR(50)     NOT NULL,
                                     label       VARCHAR(100)    NOT NULL,
                                     sort_order  SMALLINT        NOT NULL DEFAULT 0,
                                     enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
                                     created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT pk_system_config_group PRIMARY KEY (group_code)
);

COMMENT ON TABLE system_config_group IS '설정 페이지 좌측 메뉴(그룹) - 관리자 전용';
COMMENT ON COLUMN system_config_group.group_code IS '그룹 코드 (예: LOGIN, BOARD)';
COMMENT ON COLUMN system_config_group.label IS '메뉴에 표시될 이름 (예: 로그인, 게시판)';
COMMENT ON COLUMN system_config_group.sort_order IS '좌측 메뉴 정렬 순서 (낮을수록 위)';
COMMENT ON COLUMN system_config_group.enabled IS '활성 여부 (false면 메뉴에 노출 안 됨)';


-- 설정 항목 (key-value)
CREATE TABLE system_config_item (
                                    config_key   VARCHAR(100)    NOT NULL,
                                    group_code   VARCHAR(50)     NOT NULL,
                                    label        VARCHAR(100)    NOT NULL,
                                    value        VARCHAR(255)    NOT NULL,
                                    value_type   VARCHAR(10)     NOT NULL,           -- TEXT, LIST
                                    sort_order   SMALLINT        NOT NULL DEFAULT 0,
                                    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT pk_system_config_item PRIMARY KEY (config_key),
                                    CONSTRAINT fk_system_config_item_group FOREIGN KEY (group_code) REFERENCES system_config_group (group_code)
);

COMMENT ON TABLE system_config_item IS '설정 항목 (key-value) - 관리자 전용';
COMMENT ON COLUMN system_config_item.config_key IS '설정 키 (예: LOGIN_MAX_FAIL_COUNT)';
COMMENT ON COLUMN system_config_item.group_code IS '소속 그룹 코드';
COMMENT ON COLUMN system_config_item.label IS '설정 화면에 표시될 항목 설명 (예: 로그인 실패 허용 횟수)';
COMMENT ON COLUMN system_config_item.value IS '설정값. TEXT는 단일 값, LIST는 콤마(,)로 구분된 값';
COMMENT ON COLUMN system_config_item.value_type IS '값 타입 (TEXT: 단일 텍스트/숫자, LIST: 콤마 구분 목록)';
COMMENT ON COLUMN system_config_item.sort_order IS '그룹 내 항목 정렬 순서 (낮을수록 위)';

CREATE INDEX idx_system_config_item_group_code ON system_config_item (group_code);


-- ===== 초기 데이터 =====

INSERT INTO system_config_group (group_code, label, sort_order)
VALUES
    ('LOGIN', '로그인', 1),
    ('BOARD', '게시판', 2);

INSERT INTO system_config_item (config_key, group_code, label, value, value_type, sort_order)
VALUES
    ('LOGIN_MAX_FAIL_COUNT', 'LOGIN', '로그인 실패 허용 횟수', '5', 'TEXT', 1),
    ('NOTICE_MAX_COUNT', 'BOARD', '공지 최대 노출 개수', '5', 'TEXT', 1),
    ('POST_PAGE_SIZE_OPTIONS', 'BOARD', '페이지 크기 선택지', '10,20,50,100', 'LIST', 2);