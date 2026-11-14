package com.hyunsu.springboot.member.presentation.dto.response;

/**
 * 이메일 인증코드 검증 응답.
 *
 * @param verified          인증코드 일치 여부
 * @param verificationToken 인증 성공 시 발급된 토큰 (실패 시 null). 회원가입 요청에 함께 보내야 한다.
 */
public record EmailCodeVerifyResponse(
        boolean verified,
        String verificationToken
) {
}
