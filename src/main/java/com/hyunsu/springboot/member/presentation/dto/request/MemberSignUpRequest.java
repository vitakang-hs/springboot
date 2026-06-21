package com.hyunsu.springboot.member.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청.
 *
 * emailCode는 받지 않는다 - 화면(JS)에서 이미 /api/members/email/verify-code로
 * 인증을 완료한 뒤에만 회원가입 버튼이 활성화되는 흐름이라, 서버는 그 결과를 신뢰하고 진행한다.
 * (재검증하지 않는 정책. 단, 인증코드 자체는 검증 성공 시 Redis에서 즉시 삭제되므로 재사용 불가)
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
        String email
) {
}