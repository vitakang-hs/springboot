package com.hyunsu.springboot.global.exception;

/**
 * 에러 발생 시 ApiResponse.fail()에 전달할 코드/메시지를 묶어주는 헬퍼.
 * GlobalExceptionHandler에서 ErrorCode -> ApiResponse 변환 시 사용.
 */
public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message);
    }
}