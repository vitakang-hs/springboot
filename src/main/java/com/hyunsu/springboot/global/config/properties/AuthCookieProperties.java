package com.hyunsu.springboot.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * hyunsu.auth.cookie.* 설정값.
 *
 * application.yml(공통) 예:
 *   hyunsu:
 *     auth:
 *       cookie:
 *         secure: false   # 로컬/개발(HTTP)은 false, 운영(HTTPS)은 true로 프로필별 오버라이드
 */
@ConfigurationProperties(prefix = "hyunsu.auth.cookie")
public record AuthCookieProperties(
        boolean secure
) {
}