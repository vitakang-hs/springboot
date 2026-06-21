package com.hyunsu.springboot.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * hyunsu.crypto.* 설정값.
 *
 * application-local.yml 예:
 *   hyunsu:
 *     crypto:
 *       aes:
 *         secret: ...
 *         salt: ...
 *       hash:
 *         salt: ...
 */
@ConfigurationProperties(prefix = "hyunsu.crypto")
public record CryptoProperties(
        Aes aes,
        Hash hash
) {
    public record Aes(String secret, String salt) {
    }

    public record Hash(String salt) {
    }
}