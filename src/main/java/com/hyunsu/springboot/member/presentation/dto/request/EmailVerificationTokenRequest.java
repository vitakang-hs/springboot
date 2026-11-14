package com.hyunsu.springboot.member.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 인증 완료 증명 토큰으로 이메일 조회 요청.
 * 토큰을 URL 파라미터가 아닌 요청 바디로 전송해 서버 접근 로그 노출을 줄인다.
 */
public record EmailVerificationTokenRequest(
        @NotBlank String token
) {
}
