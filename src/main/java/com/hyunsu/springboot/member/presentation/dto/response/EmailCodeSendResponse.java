package com.hyunsu.springboot.member.presentation.dto.response;

/**
 * 이메일 인증코드 발송 응답.
 *
 * 주의: code 필드는 테스트 편의를 위한 임시 조치입니다.
 * 실제 메일 발송(SMTP) 연동이 완료되면 이 필드는 응답에서 제거해야 합니다.
 * (운영 환경에서 인증코드를 클라이언트로 그대로 내려주는 것은 보안 위반입니다.)
 */
public record EmailCodeSendResponse(String code) {
}