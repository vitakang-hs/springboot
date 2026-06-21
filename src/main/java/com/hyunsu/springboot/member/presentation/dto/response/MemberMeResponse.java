package com.hyunsu.springboot.member.presentation.dto.response;

/**
 * 로그인한 본인 정보 조회 응답.
 * username은 소셜 로그인 전용 계정이면 null일 수 있다.
 */
public record MemberMeResponse(
        Long id,
        String username,
        String email
) {
}