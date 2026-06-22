-- ============================================================
-- V5__add_board.sql
-- 게시글(post), 댓글(comment) 테이블 생성
-- ============================================================

-- 게시글 테이블 (Aggregate Root)
CREATE TABLE post (
                      id          BIGSERIAL       PRIMARY KEY,
                      member_id   BIGINT          NOT NULL,
                      category    VARCHAR(50)     NOT NULL,           -- code 테이블의 (POST_CATEGORY, code)와 매핑
                      title       VARCHAR(200)    NOT NULL,
                      content     TEXT            NOT NULL,
                      view_count  INTEGER         NOT NULL DEFAULT 0,
                      created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      deleted_at  TIMESTAMP,                           -- soft delete

                      CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member (id)
);

COMMENT ON TABLE post IS '게시글 (Aggregate Root)';
COMMENT ON COLUMN post.category IS 'code 테이블의 POST_CATEGORY 그룹 코드값 (NOTICE, FREE 등)';
COMMENT ON COLUMN post.view_count IS '조회수 (읽기 요청마다 증가)';
COMMENT ON COLUMN post.deleted_at IS '삭제 시각 (soft delete, NULL이면 정상)';

CREATE INDEX idx_post_member_id  ON post (member_id);
CREATE INDEX idx_post_category   ON post (category);
CREATE INDEX idx_post_created_at ON post (created_at DESC);


-- 댓글 테이블 (Post Aggregate에 종속)
-- 댓글은 게시글 없이 독립적으로 존재할 수 없으므로 Aggregate 경계 안에 포함.
-- 단, 별도 Repository(CommentRepository)로 접근한다.
CREATE TABLE comment (
                         id          BIGSERIAL       PRIMARY KEY,
                         post_id     BIGINT          NOT NULL,
                         member_id   BIGINT          NOT NULL,
                         content     VARCHAR(1000)   NOT NULL,
                         created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at  TIMESTAMP,                           -- soft delete

                         CONSTRAINT fk_comment_post   FOREIGN KEY (post_id)   REFERENCES post (id),
                         CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member (id)
);

COMMENT ON TABLE comment IS '댓글 (Post Aggregate 내부)';
COMMENT ON COLUMN comment.deleted_at IS '삭제 시각 (soft delete, NULL이면 정상)';

CREATE INDEX idx_comment_post_id ON comment (post_id);