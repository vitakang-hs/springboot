package com.hyunsu.springboot.member.infrastructure.mail;

import com.hyunsu.springboot.global.config.properties.MailSenderProperties;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.member.domain.service.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * EmailSender의 SMTP 구현체.
 * spring.mail.* 설정(호스트, 계정)으로 Spring Boot가 만들어주는 JavaMailSender를 사용한다.
 *
 * 빈 등록은 EmailSenderConfig가 담당한다 (spring.mail.host가 있을 때만).
 */
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final MailSenderProperties properties;

    public SmtpEmailSender(JavaMailSender mailSender, MailSenderProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(to);
        message.setSubject("[" + properties.serviceName() + "] 이메일 인증코드");
        message.setText("""
                안녕하세요.

                회원가입 이메일 인증코드는 아래와 같습니다.

                    %s

                인증코드는 3분 동안 유효합니다.
                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                """.formatted(code));

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("인증코드 메일 발송 실패", e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
