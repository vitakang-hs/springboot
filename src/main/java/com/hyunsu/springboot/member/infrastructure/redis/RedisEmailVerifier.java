package com.hyunsu.springboot.member.infrastructure.redis;

import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * EmailVerifier의 Redis 구현체.
 *
 * 인증코드:      key "email-verification-code:{이메일}"     → 6자리 코드, TTL 3분
 * 재발급 대기:   key "email-verification-cooldown:{이메일}" → "1",        TTL 1분 (메일 폭탄 방지)
 * 실패 횟수:     key "email-verification-attempts:{이메일}" → 실패 횟수,  TTL 3분 (5회 실패 시 코드 폐기)
 * 인증완료 토큰: key "email-verified-token:{UUID}"          → 이메일,      TTL 30분
 *
 * 인증코드 key는 이메일 원문을 그대로 사용한다. 인증코드는 TTL 3분짜리 휘발성 데이터라,
 * 영구 저장되는 회원 DB의 email_hash(검색/중복확인용)와는 성격이 다르므로 굳이 해시로 감추지 않는다.
 *
 * 인증완료 토큰 TTL(30분)은 인증 후 아이디/비밀번호를 입력하고 가입 버튼을 누르기까지의 여유 시간이다.
 * 만료되면 이메일 인증(또는 구글 로그인)부터 다시 진행해야 한다.
 *
 * 이 클래스는 코드 발급/검증만 책임지고, 메일 발송은 EmailSender가 담당한다.
 * 인증코드는 절대 API 응답으로 클라이언트에 내려주지 않는다.
 */
@Service
public class RedisEmailVerifier implements EmailVerifier {

    private static final String CODE_KEY_PREFIX = "email-verification-code:";
    private static final String COOLDOWN_KEY_PREFIX = "email-verification-cooldown:";
    private static final String ATTEMPTS_KEY_PREFIX = "email-verification-attempts:";
    private static final String TOKEN_KEY_PREFIX = "email-verified-token:";
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final int CODE_LENGTH = 6;

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public RedisEmailVerifier(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<String> issueCode(String email) {
        // SET NX: 대기시간 키가 없을 때만 생성 → 동시 요청이 와도 한 번만 통과
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(cooldownKey(email), "1", RESEND_COOLDOWN);
        if (!Boolean.TRUE.equals(acquired)) {
            return Optional.empty();
        }

        String code = generateCode();
        redisTemplate.opsForValue().set(codeKey(email), code, CODE_TTL);
        redisTemplate.delete(attemptsKey(email)); // 새 코드는 실패 횟수 0부터
        return Optional.of(code);
    }

    @Override
    public boolean verifyCode(String email, String inputCode) {
        String savedCode = redisTemplate.opsForValue().get(codeKey(email));

        if (savedCode == null) {
            return false;
        }

        if (savedCode.equals(inputCode)) {
            redisTemplate.delete(List.of(codeKey(email), attemptsKey(email)));
            return true;
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey(email));
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey(email), CODE_TTL);
        }
        if (attempts != null && attempts >= MAX_VERIFY_ATTEMPTS) {
            redisTemplate.delete(List.of(codeKey(email), attemptsKey(email)));
        }
        return false;
    }

    @Override
    public String issueVerificationToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(tokenKey(token), email, TOKEN_TTL);
        return token;
    }

    @Override
    public Optional<String> findVerifiedEmail(String verificationToken) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(tokenKey(verificationToken)));
    }

    @Override
    public void consumeVerificationToken(String verificationToken) {
        redisTemplate.delete(tokenKey(verificationToken));
    }

    private String generateCode() {
        int number = secureRandom.nextInt((int) Math.pow(10, CODE_LENGTH));
        return String.format("%0" + CODE_LENGTH + "d", number);
    }

    private String codeKey(String email) {
        return CODE_KEY_PREFIX + email;
    }

    private String cooldownKey(String email) {
        return COOLDOWN_KEY_PREFIX + email;
    }

    private String attemptsKey(String email) {
        return ATTEMPTS_KEY_PREFIX + email;
    }

    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
