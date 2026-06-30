-- ============================================================
-- V6__restrict_oauth_signup_policy.sql
-- 정책 변경: 구글 로그인만으로는 더 이상 회원이 자동 생성되지 않는다.
--
-- 배경:
--   V3에서는 OAuth 전용 가입(Member.registerByOAuth())을 허용하기 위해
--   username/password를 nullable로 풀었다.
--
--   이후 정책이 바뀌어, 구글 로그인 시 DB에 일치하는 회원이 없으면
--   회원을 자동 생성하지 않고 회원가입 페이지로 리다이렉트하여
--   사용자가 직접 아이디/비밀번호를 입력하도록 변경했다
--   (이메일 인증은 이미 구글에서 검증되었으므로 스킵).
--
--   즉, 가입이 완료된 모든 회원은 항상 username/password를 갖게 되므로
--   nullable로 풀었던 제약을 다시 NOT NULL로 되돌린다.
--
-- 주의: 적용 전 username 또는 password가 NULL인 회원 데이터가
--       남아있으면 안 된다. (운영 환경이라면 데이터 정리/마이그레이션이
--       선행되어야 함. 본 프로젝트는 개발 단계에서 해당 데이터 없음을 확인 후 적용)
-- ============================================================

ALTER TABLE member ALTER COLUMN username SET NOT NULL;
ALTER TABLE member ALTER COLUMN password SET NOT NULL;

COMMENT ON COLUMN member.username IS '로그인 아이디 (평문). 모든 회원이 보유 (V6부터 OAuth 전용 가입 불가)';
COMMENT ON COLUMN member.password IS 'Bcrypt 단방향 암호화 비밀번호. 모든 회원이 보유 (V6부터 OAuth 전용 가입 불가)';