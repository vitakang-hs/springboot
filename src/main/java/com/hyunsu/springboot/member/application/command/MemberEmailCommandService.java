package com.hyunsu.springboot.member.application.command;

import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommand;
import com.hyunsu.springboot.member.application.command.dto.EmailCodeVerifyCommandResult;
import com.hyunsu.springboot.member.domain.service.EmailSender;
import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 이메일 인증 관련 Command (상태를 변경하는 행위) 처리.
 *
 * infrastructure의 구체 구현(RedisEmailVerifier)을 직접 의존하지 않고,
 * domain.service의 EmailVerifier / EmailSender 인터페이스에만 의존한다 (의존성 역전).
 */
@Service
@RequiredArgsConstructor
public class MemberEmailCommandService {

    private final EmailVerifier emailVerifier;
    private final EmailSender emailSender;

    /**
     * 인증코드를 발급해 메일로 발송한다. 코드는 반환하지 않는다 (클라이언트 노출 금지).
     */
    public void sendVerificationCode(String email) {
        String code = emailVerifier.issueCode(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_CODE_RESEND_TOO_SOON));
        emailSender.sendVerificationCode(email, code);
    }

    /**
     * 인증코드 검증. 성공하면 회원가입 시 제출할 인증 완료 증명 토큰을 발급한다.
     */
    public EmailCodeVerifyCommandResult verifyCode(EmailCodeVerifyCommand command) {
        boolean verified = emailVerifier.verifyCode(command.email(), command.code());
        if (!verified) {
            return EmailCodeVerifyCommandResult.failure();
        }
        String token = emailVerifier.issueVerificationToken(command.email());
        return EmailCodeVerifyCommandResult.success(token);
    }
}
