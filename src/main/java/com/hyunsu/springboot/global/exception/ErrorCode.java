package com.hyunsu.springboot.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 전역에서 사용하는 에러 코드 정의.
 * 도메인이 늘어날수록 이 Enum도 함께 늘어나는 구조 (도메인별 prefix로 구분: MEMBER_xxx, AUTH_xxx, BOARD_xxx)
 */
@Getter
public enum ErrorCode {

    // ===== Common (공통) =====
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_003", "허용되지 않은 HTTP 메서드입니다."),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_004", "요청하신 API를 찾을 수 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_003", "요청한 리소스를 찾을 수 없습니다."),

    // ===== Member (회원) =====
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "존재하지 않는 회원입니다."),
    USERNAME_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_002", "이미 사용 중인 아이디입니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "MEMBER_003", "이미 사용 중인 이메일입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "MEMBER_004", "이메일 인증이 완료되지 않았습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "MEMBER_005", "이메일 인증이 만료되었습니다. 다시 인증해 주세요."),
    EMAIL_CODE_RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "MEMBER_006", "인증코드는 1분에 한 번만 발송할 수 있습니다. 잠시 후 다시 시도해 주세요."),
    EMAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "MEMBER_007", "인증코드 메일 발송에 실패했습니다. 잠시 후 다시 시도해 주세요."),

    // ===== Auth (인증) =====
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "AUTH_002", "로그인 실패 횟수를 초과하여 계정이 잠겼습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}