package com.hyunsu.springboot.member.presentation.dto.response;

/**
 * 인증 완료 증명 토큰으로 조회한 이메일 응답.
 * signup.js가 구글 가입 진입 시 이 값을 받아 이메일 input에 채운다.
 */
public record VerifiedEmailResponse(
        String email
) {
}
