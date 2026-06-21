package com.hyunsu.springboot.member.application.command;

import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommand;
import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 이메일 인증 관련 Command (상태를 변경하는 행위) 처리.
 *
 * infrastructure의 구체 구현(RedisEmailVerifier)을 직접 의존하지 않고,
 * domain.service의 EmailVerifier 인터페이스에만 의존한다 (의존성 역전).
 */
@Service
@RequiredArgsConstructor
public class MemberEmailCommandService {

    private final EmailVerifier emailVerifier;

    public String sendVerificationCode(String email) {
        return emailVerifier.issueCode(email);
    }

    public boolean verifyCode(EmailCodeVerifyCommand command) {
        return emailVerifier.verifyCode(command.email(), command.code());
    }
}