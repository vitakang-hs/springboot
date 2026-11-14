package com.hyunsu.springboot.member.infrastructure.redis;

import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * EmailVerifier의 Redis 구현체.
 *
 * 인증코드:      key "email-verification-code:{이메일}" → 6자리 코드, TTL 3분
 * 인증완료 토큰: key "email-verified-token:{UUID}"      → 이메일,      TTL 30분
 *
 * 인증코드 key는 이메일 원문을 그대로 사용한다. 인증코드는 TTL 3분짜리 휘발성 데이터라,
 * 영구 저장되는 회원 DB의 email_hash(검색/중복확인용)와는 성격이 다르므로 굳이 해시로 감추지 않는다.
 *
 * 인증완료 토큰 TTL(30분)은 인증 후 아이디/비밀번호를 입력하고 가입 버튼을 누르기까지의 여유 시간이다.
 * 만료되면 이메일 인증(또는 구글 로그인)부터 다시 진행해야 한다.
 *
 * 현재 단계에서는 실제 메일 발송(SMTP) 연동 전이라, 발급된 코드를 호출부(Controller)가
 * 응답으로 그대로 내려준다. 운영 환경에서는 절대 코드값을 클라이언트에 노출해서는 안 된다.
 */
@Service
public class RedisEmailVerifier implements EmailVerifier {

    private static final String CODE_KEY_PREFIX = "email-verification-code:";
    private static final String TOKEN_KEY_PREFIX = "email-verified-token:";
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final int CODE_LENGTH = 6;

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public RedisEmailVerifier(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String issueCode(String email) {
        String code = generateCode();
        redisTemplate.opsForValue().set(codeKey(email), code, CODE_TTL);
        return code;
    }

    @Override
    public boolean verifyCode(String email, String inputCode) {
        String savedCode = redisTemplate.opsForValue().get(codeKey(email));

        if (savedCode == null) {
            return false;
        }

        boolean matched = savedCode.equals(inputCode);
        if (matched) {
            redisTemplate.delete(codeKey(email));
        }
        return matched;
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

    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
