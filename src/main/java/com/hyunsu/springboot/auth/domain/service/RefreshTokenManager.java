package com.hyunsu.springboot.auth.domain.service;

import java.time.Duration;

/**
 * Refresh Token 저장/검증/삭제 추상화.
 *
 * application 레이어는 이 인터페이스에만 의존하고, 실제로 Redis로 구현되어 있다는 사실은
 * infrastructure.redis.RedisRefreshTokenManager만 알고 있다 (의존성 역전 원칙).
 */
public interface RefreshTokenManager {

    /**
     * Refresh Token 저장. 같은 memberId로 이미 저장된 토큰이 있다면 덮어쓴다.
     * (한 회원당 유효한 토큰은 항상 하나만 존재 -> 중복 로그인 방지)
     */
    void save(Long memberId, String refreshToken, Duration validity);

    /**
     * 저장된 Refresh Token과 일치하는지 검증.
     */
    boolean isValid(Long memberId, String refreshToken);

    /**
     * Refresh Token 삭제 (로그아웃, 강제 만료 시 사용).
     */
    void delete(Long memberId);
}