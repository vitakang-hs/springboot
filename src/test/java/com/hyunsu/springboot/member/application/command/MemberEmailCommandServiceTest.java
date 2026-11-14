package com.hyunsu.springboot.member.application.command;

import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommand;
import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommandResult;
import com.hyunsu.springboot.member.domain.service.EmailSender;
import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberEmailCommandServiceTest {

    @Mock EmailVerifier emailVerifier;
    @Mock EmailSender emailSender;
    @InjectMocks MemberEmailCommandService service;

    @Test
    @DisplayName("인증코드를 발급해 메일로 발송한다")
    void sendVerificationCode_sendsMail() {
        given(emailVerifier.issueCode("user@example.com")).willReturn(Optional.of("123456"));

        service.sendVerificationCode("user@example.com");

        verify(emailSender).sendVerificationCode("user@example.com", "123456");
    }

    @Test
    @DisplayName("재발급 대기시간 중이면 발송하지 않고 예외를 던진다")
    void sendVerificationCode_tooSoon() {
        given(emailVerifier.issueCode("user@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendVerificationCode("user@example.com"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EMAIL_CODE_RESEND_TOO_SOON);

        verify(emailSender, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("인증코드가 맞으면 인증 완료 증명 토큰을 발급한다")
    void verifyCode_success_issuesToken() {
        given(emailVerifier.verifyCode("user@example.com", "123456")).willReturn(true);
        given(emailVerifier.issueVerificationToken("user@example.com")).willReturn("token");

        EmailCodeVerifyCommandResult result =
                service.verifyCode(new EmailCodeVerifyCommand("user@example.com", "123456"));

        assertThat(result.verified()).isTrue();
        assertThat(result.verificationToken()).isEqualTo("token");
    }

    @Test
    @DisplayName("인증코드가 틀리면 토큰을 발급하지 않는다")
    void verifyCode_failure_noToken() {
        given(emailVerifier.verifyCode("user@example.com", "000000")).willReturn(false);

        EmailCodeVerifyCommandResult result =
                service.verifyCode(new EmailCodeVerifyCommand("user@example.com", "000000"));

        assertThat(result.verified()).isFalse();
        assertThat(result.verificationToken()).isNull();
        verify(emailVerifier, never()).issueVerificationToken(anyString());
    }
}
