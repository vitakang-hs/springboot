package com.hyunsu.springboot.member.infrastructure.redis;

import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * EmailVerifier의 Redis 구현체.
 *
 * Redis key는 "email-verification-code:{이메일}" 형태로, 이메일 원문을 그대로 사용한다.
 * 인증코드는 TTL 3분짜리 휘발성 데이터라, 영구 저장되는 회원 DB의 email_hash(검색/중복확인용)와는
 * 성격이 다르므로 굳이 해시로 감추지 않는다.
 *
 * 현재 단계에서는 실제 메일 발송(SMTP) 연동 전이라, 발급된 코드를 호출부(Controller)가
 * 응답으로 그대로 내려준다. 운영 환경에서는 절대 코드값을 클라이언트에 노출해서는 안 된다.
 */
@Service
public class RedisEmailVerifier implements EmailVerifier {

    private static final String KEY_PREFIX = "email-verification-code:";
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final int CODE_LENGTH = 6;

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public RedisEmailVerifier(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String issueCode(String email) {
        String code = generateCode();
        redisTemplate.opsForValue().set(redisKey(email), code, CODE_TTL);
        return code;
    }

    @Override
    public boolean verifyCode(String email, String inputCode) {
        String savedCode = redisTemplate.opsForValue().get(redisKey(email));

        if (savedCode == null) {
            return false;
        }

        boolean matched = savedCode.equals(inputCode);
        if (matched) {
            redisTemplate.delete(redisKey(email));
        }
        return matched;
    }

    private String generateCode() {
        int number = secureRandom.nextInt((int) Math.pow(10, CODE_LENGTH));
        return String.format("%0" + CODE_LENGTH + "d", number);
    }

    private String redisKey(String email) {
        return KEY_PREFIX + email;
    }
}