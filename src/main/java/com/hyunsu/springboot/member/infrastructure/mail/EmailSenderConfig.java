package com.hyunsu.springboot.member.infrastructure.mail;

import com.hyunsu.springboot.global.config.properties.MailSenderProperties;
import com.hyunsu.springboot.member.domain.service.EmailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 환경에 따라 EmailSender 구현체를 선택한다.
 *
 *  - spring.mail.host 설정 O → SmtpEmailSender (실제 발송)
 *  - spring.mail.host 설정 X → LoggingEmailSender (로그 출력, 로컬 개발용)
 *
 * 같은 클래스 안의 @Bean은 선언 순서대로 등록되므로, smtp가 먼저 등록되면
 * @ConditionalOnMissingBean에 의해 logging은 등록되지 않는다.
 */
@Configuration
public class EmailSenderConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.mail", name = "host")
    public EmailSender smtpEmailSender(JavaMailSender mailSender, MailSenderProperties properties) {
        return new SmtpEmailSender(mailSender, properties);
    }

    @Bean
    @ConditionalOnMissingBean(EmailSender.class)
    public EmailSender loggingEmailSender() {
        return new LoggingEmailSender();
    }
}
