package com.hyunsu.springboot.member.domain.service;

/**
 * 이메일 발송 추상화.
 *
 * application 레이어는 이 인터페이스에만 의존하고, 실제 발송 방식(SMTP, 로그 출력 등)은
 * infrastructure.mail의 구현체가 결정한다 (의존성 역전 - EmailVerifier와 같은 패턴).
 *
 * 구현체 선택 기준 (member.infrastructure.mail.EmailSenderConfig):
 *  - spring.mail.host 설정이 있으면 SmtpEmailSender (실제 메일 발송)
 *  - 없으면 LoggingEmailSender (서버 로그에만 출력 - SMTP 계정 없이 로컬 개발할 때)
 */
public interface EmailSender {

    /**
     * 이메일 인증코드를 발송한다.
     */
    void sendVerificationCode(String to, String code);
}
