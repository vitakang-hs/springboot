package com.hyunsu.springboot.member.application.command;

import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.global.security.crypto.AesGcmCryptoService;
import com.hyunsu.springboot.global.security.crypto.EmailHasher;
import com.hyunsu.springboot.member.application.command.dto.MemberRegisterCommand;
import com.hyunsu.springboot.member.domain.model.entity.Member;
import com.hyunsu.springboot.member.domain.repository.MemberRepository;
import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberRegisterCommandServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String TOKEN = "verification-token";

    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AesGcmCryptoService aesGcmCryptoService;
    @Mock EmailHasher emailHasher;
    @Mock EmailVerifier emailVerifier;

    @InjectMocks MemberRegisterCommandService service;

    @BeforeEach
    void setUp() {
        lenient().when(emailHasher.hash(anyString())).thenReturn("hash");
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        lenient().when(aesGcmCryptoService.encrypt(anyString())).thenReturn("encrypted");
    }

    private MemberRegisterCommand command(String email, String token) {
        return new MemberRegisterCommand("tester", "password1", email, token);
    }

    @Test
    @DisplayName("인증 토큰이 가리키는 이메일과 가입 이메일이 같으면 가입되고 토큰은 소비된다")
    void register_success() {
        given(emailVerifier.findVerifiedEmail(TOKEN)).willReturn(Optional.of(EMAIL));

        service.register(command(EMAIL, TOKEN));

        verify(memberRepository).save(any(Member.class));
        verify(emailVerifier).consumeVerificationToken(TOKEN);
    }

    @Test
    @DisplayName("인증 토큰이 없거나 만료되면 가입할 수 없다")
    void register_tokenExpired() {
        given(emailVerifier.findVerifiedEmail(TOKEN)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(command(EMAIL, TOKEN)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EMAIL_VERIFICATION_EXPIRED);

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("다른 이메일로 받은 인증 토큰으로는 가입할 수 없다 (남의 이메일로 가입 방지)")
    void register_emailMismatch() {
        given(emailVerifier.findVerifiedEmail(TOKEN)).willReturn(Optional.of("attacker@example.com"));

        assertThatThrownBy(() -> service.register(command(EMAIL, TOKEN)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);

        verify(memberRepository, never()).save(any());
        verify(emailVerifier, never()).consumeVerificationToken(anyString());
    }

    @Test
    @DisplayName("아이디가 중복되면 가입이 실패하고, 토큰은 남겨두어 재시도 시 재인증이 필요 없다")
    void register_usernameDuplicated_keepsToken() {
        given(emailVerifier.findVerifiedEmail(TOKEN)).willReturn(Optional.of(EMAIL));
        given(memberRepository.existsByUsername("tester")).willReturn(true);

        assertThatThrownBy(() -> service.register(command(EMAIL, TOKEN)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USERNAME_DUPLICATED);

        verify(emailVerifier, never()).consumeVerificationToken(anyString());
    }
}
