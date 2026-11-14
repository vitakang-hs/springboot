package com.hyunsu.springboot.member.application.command.dto;

/**
 * 이메일 인증코드 검증 결과 (application 내부 표현).
 *
 * @param verified          인증코드 일치 여부
 * @param verificationToken 인증 성공 시 발급된 인증 완료 증명 토큰 (실패 시 null).
 *                          회원가입 요청 시 함께 보내야 한다.
 */
public record EmailCodeVerifyCommandResult(
        boolean verified,
        String verificationToken
) {

    public static EmailCodeVerifyCommandResult success(String verificationToken) {
        return new EmailCodeVerifyCommandResult(true, verificationToken);
    }

    public static EmailCodeVerifyCommandResult failure() {
        return new EmailCodeVerifyCommandResult(false, null);
    }
}
