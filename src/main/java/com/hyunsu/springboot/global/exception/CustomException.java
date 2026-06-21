package com.hyunsu.springboot.global.exception;

import lombok.Getter;

/**
 * 모든 도메인 커스텀 예외의 베이스 클래스.
 *
 * 사용 예 (member 도메인):
 *   public class MemberNotFoundException extends CustomException {
 *       public MemberNotFoundException() {
 *           super(ErrorCode.MEMBER_NOT_FOUND);
 *       }
 *   }
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}