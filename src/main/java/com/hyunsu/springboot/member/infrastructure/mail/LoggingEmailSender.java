package com.hyunsu.springboot.member.infrastructure.mail;

import com.hyunsu.springboot.member.domain.service.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EmailSender의 로그 출력 구현체 (로컬 개발용).
 *
 * SMTP 계정(spring.mail.*)을 설정하지 않은 환경에서는 실제 메일 대신 서버 로그에 인증코드를 출력한다.
 * 인증코드는 API 응답으로 내려주지 않는다 - 서버 콘솔을 볼 수 있는 개발자만 확인 가능.
 *
 * 빈 등록은 EmailSenderConfig가 담당한다 (spring.mail.host가 없을 때만).
 * 운영 환경에서는 반드시 spring.mail.*을 설정해 SmtpEmailSender가 쓰이도록 해야 한다.
 */
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    public LoggingEmailSender() {
        log.warn("spring.mail.host가 설정되지 않아 이메일을 실제로 발송하지 않고 로그로만 출력합니다 (로컬 개발용).");
    }

    @Override
    public void sendVerificationCode(String to, String code) {
        log.info("[메일 미발송 - 로컬 개발용] 인증코드: to={}, code={}", to, code);
    }
}
