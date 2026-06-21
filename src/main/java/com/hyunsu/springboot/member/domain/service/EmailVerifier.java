package com.hyunsu.springboot.member.domain.service;

/**
 * 이메일 인증코드 발급/검증 추상화.
 *
 * application 레이어는 이 인터페이스에만 의존하고, 실제로 Redis로 구현되어 있다는 사실은
 * infrastructure.redis.RedisEmailVerifier만 알고 있다 (의존성 역전 원칙).
 *
 * Repository와 동일한 패턴: domain에 인터페이스, infrastructure에 구현체.
 */
public interface EmailVerifier {

    /**
     * 인증코드 발급.
     * @return 발급된 6자리 코드
     */
    String issueCode(String email);

    /**
     * 인증코드 검증.
     */
    boolean verifyCode(String email, String code);
}