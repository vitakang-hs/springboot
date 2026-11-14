package com.hyunsu.springboot.member.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청.
 *
 * emailVerificationToken: 이메일 인증을 통과했다는 증명 토큰.
 *  - 일반 가입: /api/members/email/verify-code 성공 응답으로 받은 값
 *  - 구글 가입: OAuth2LoginSuccessHandler가 회원가입 페이지 URL(?token=)에 담아준 값
 * 서버는 화면(JS)의 인증 완료 상태를 믿지 않고, 이 토큰이 가리키는 이메일과 요청 email이
 * 일치하는지 직접 확인한다 (MemberRegisterCommandService).
 */
public record MemberSignUpRequest(

        @NotBlank(message = "아이디를 입력해 주세요.")
        @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "아이디는 영문/숫자 4~20자여야 합니다.")
        String username,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "이메일 인증을 먼저 완료해 주세요.")
        String emailVerificationToken
) {
}
