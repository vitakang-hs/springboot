package com.hyunsu.springboot.member.application.command;

import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommand;
import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommandResult;
import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberEmailCommandServiceTest {

    @Mock EmailVerifier emailVerifier;
    @InjectMocks MemberEmailCommandService service;

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
