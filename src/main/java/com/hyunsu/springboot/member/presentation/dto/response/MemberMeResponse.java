package com.hyunsu.springboot.member.presentation.dto.response;

/**
 * 로그인한 본인 정보 조회 응답.
 * role: USER / ADMIN - 화면에서 관리자 전용 메뉴/선택지 노출 여부를 판단한다 (실제 권한 검증은 서버가 한다).
 */
public record MemberMeResponse(
        Long id,
        String username,
        String email,
        String role
) {
}