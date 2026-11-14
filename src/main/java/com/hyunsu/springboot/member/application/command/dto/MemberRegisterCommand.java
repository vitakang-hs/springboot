package com.hyunsu.springboot.member.application.command.dto;

/**
 * 회원가입 Command.
 *
 * MemberSignUpRequest(presentation, HTTP 요청 모양)와 분리된, "회원가입을 수행하라"는
 * 도메인 의도를 표현하는 객체. Service는 이 타입만 알면 되고, presentation의 Request DTO를
 * 직접 의존하지 않는다.
 *
 * 지금은 필드 구성이 Request와 동일하지만, 향후 Request 쪽에 검증 전용 필드
 * (예: passwordConfirm)가 추가되어도 Command는 영향받지 않는다.
 */
public record MemberRegisterCommand(
        String username,
        String password,
        String email,
        String emailVerificationToken
) {
}