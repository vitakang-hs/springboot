package com.hyunsu.springboot.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 모든 API 응답을 감싸는 공통 포맷.
 *
 * 성공: { "success": true, "status": 200, "data": ..., "timestamp": ... }
 * 실패: { "success": false, "status": 409, "code": "MEMBER_002", "message": "...", "timestamp": ... }
 *
 * status: HTTP 상태 코드(숫자). 성공/실패 관계없이 항상 채워진다.
 *         (클라이언트가 success boolean뿐 아니라 정확한 상태코드도 바로 확인할 수 있도록)
 * code:   실패 시에만 채워지는 프로젝트 자체 에러 코드 문자열 (ErrorCode 참고). 성공 시 응답에서 생략됨.
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final int status;
    private final T data;
    private final String message;
    private final String code;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, int status, T data, String message, String code) {
        this.success = success;
        this.status = status;
        this.data = data;
        this.message = message;
        this.code = code;
        // 정적 팩토리로 생성되는 값 객체라 Clock 빈을 주입받을 수 없어, 기준 시간대(서버 기본)를 명시한다
        this.timestamp = LocalDateTime.now(ZoneId.systemDefault());
    }

    // ===== 성공 응답 =====

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, HttpStatus.OK.value(), data, null, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, HttpStatus.OK.value(), null, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, HttpStatus.OK.value(), data, message, null);
    }

    /** 상태코드를 직접 지정해야 할 때 (예: 201 Created), 데이터가 있는 경우 */
    public static <T> ApiResponse<T> success(HttpStatus status, T data) {
        return new ApiResponse<>(true, status.value(), data, null, null);
    }

    /** 상태코드를 직접 지정해야 하면서, 응답 데이터는 없는 경우 (예: 회원가입 성공 -> 201, body 없음) */
    public static ApiResponse<Void> success(HttpStatus status) {
        return new ApiResponse<>(true, status.value(), null, null, null);
    }

    // ===== 실패 응답 =====

    public static ApiResponse<Void> fail(HttpStatus status, String code, String message) {
        return new ApiResponse<>(false, status.value(), null, message, code);
    }

}