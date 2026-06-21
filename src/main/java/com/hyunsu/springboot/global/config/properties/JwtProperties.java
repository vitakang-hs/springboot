package com.hyunsu.springboot.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * hyunsu.jwt.* 설정값.
 *
 * application-local.yml 예:
 *   hyunsu:
 *     jwt:
 *       secret: ...
 *       access-token-validity-minutes: 30
 *       refresh-token-validity-days: 14
 */
@ConfigurationProperties(prefix = "hyunsu.jwt")
public record JwtProperties(
        String secret,
        long accessTokenValidityMinutes,
        long refreshTokenValidityDays
) {
}