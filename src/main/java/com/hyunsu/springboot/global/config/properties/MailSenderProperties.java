package com.hyunsu.springboot.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * hyunsu.mail.* 설정값 (발송 메일의 보내는 사람/서비스명).
 * SMTP 접속 정보(호스트, 계정)는 Spring 표준인 spring.mail.*을 사용한다.
 *
 * 예:
 *   hyunsu:
 *     mail:
 *       from: no-reply@example.com
 *       service-name: hyunsu
 */
@ConfigurationProperties(prefix = "hyunsu.mail")
public record MailSenderProperties(
        @DefaultValue("no-reply@localhost") String from,
        @DefaultValue("hyunsu") String serviceName
) {
}
