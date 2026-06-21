package com.hyunsu.springboot.global.security.crypto;

import com.hyunsu.springboot.global.config.properties.CryptoProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 이메일 SHA-256 해시 생성.
 *
 * 같은 이메일이면 항상 같은 해시가 나오는 결정적(deterministic) 단방향 해시.
 * member.email_hash 컬럼(검색, 중복확인용)에 사용한다.
 *
 * AES 암호화(AesGcmCryptoService)와 달리 매번 다른 결과가 나오면 안 되므로,
 * IV 없이 고정 salt만 섞어 해시한다.
 */
@Component
public class EmailHasher {

    private final String salt;

    public EmailHasher(CryptoProperties cryptoProperties) {
        this.salt = cryptoProperties.hash().salt();
    }

    public String hash(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((email + salt).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("이메일 해시 생성 중 오류가 발생했습니다.", e);
        }
    }
}