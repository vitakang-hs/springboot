package com.hyunsu.springboot.auth.infrastructure.redis;

import com.hyunsu.springboot.auth.domain.service.RefreshTokenManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * RefreshTokenManager의 Redis 구현체.
 *
 * Redis key는 memberId 기준 (refresh-token:{memberId}).
 * 재로그인 시 새 토큰이 기존 토큰을 덮어쓰므로(SET 자체가 덮어쓰기 동작),
 * 자연스럽게 "중복 로그인 방지"가 적용된다.
 */
@Service
public class RedisRefreshTokenManager implements RefreshTokenManager {

    private static final String KEY_PREFIX = "refresh-token:";

    private final RedisTemplate<String, String> redisTemplate;

    public RedisRefreshTokenManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(Long memberId, String refreshToken, Duration validity) {
        redisTemplate.opsForValue().set(redisKey(memberId), refreshToken, validity);
    }

    @Override
    public boolean isValid(Long memberId, String refreshToken) {
        String saved = redisTemplate.opsForValue().get(redisKey(memberId));
        return saved != null && saved.equals(refreshToken);
    }

    @Override
    public void delete(Long memberId) {
        redisTemplate.delete(redisKey(memberId));
    }

    private String redisKey(Long memberId) {
        return KEY_PREFIX + memberId;
    }
}