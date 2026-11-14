package com.hyunsu.springboot.member.domain.service;

import java.util.Optional;

/**
 * 이메일 인증코드 발급/검증 + "인증 완료 증명 토큰" 관리 추상화.
 *
 * application 레이어는 이 인터페이스에만 의존하고, 실제로 Redis로 구현되어 있다는 사실은
 * infrastructure.redis.RedisEmailVerifier만 알고 있다 (의존성 역전 원칙).
 *
 * Repository와 동일한 패턴: domain에 인터페이스, infrastructure에 구현체.
 *
 * 인증 완료 증명 토큰(verification token):
 *  - 이메일 인증을 통과한 브라우저에게만 발급되는 1회용 UUID 토큰 (토큰 → 이메일 매핑).
 *  - 회원가입 API는 이 토큰을 함께 받아, "토큰이 가리키는 이메일 == 가입하려는 이메일"인지 서버에서 재확인한다.
 *    (화면(JS)의 인증 완료 상태만 믿으면, API를 직접 호출해 인증 없이 남의 이메일로 가입할 수 있기 때문)
 *  - 인증 경로와 무관하게 동일한 토큰을 사용한다.
 *      일반 가입: 인증코드 검증 성공 시 발급
 *      구글 가입: OAuth2LoginSuccessHandler가 구글이 검증한 이메일로 발급 (인증코드 단계 생략)
 */
public interface EmailVerifier {

    /**
     * 인증코드 발급.
     * 같은 이메일로 재발급 대기시간(1분) 안에 다시 요청하면 발급하지 않는다 (메일 폭탄 방지).
     * @return 발급된 6자리 코드. 재발급 대기시간 중이면 empty
     */
    Optional<String> issueCode(String email);

    /**
     * 인증코드 검증. 성공 시 코드는 즉시 삭제된다 (재사용 불가).
     * 연속으로 여러 번(5회) 틀리면 코드를 폐기한다 (무차별 대입 방지) - 이후에는 재발급이 필요하다.
     */
    boolean verifyCode(String email, String code);

    /**
     * 인증을 통과한 이메일에 대해 인증 완료 증명 토큰을 발급한다.
     * @return 발급된 토큰 (UUID)
     */
    String issueVerificationToken(String email);

    /**
     * 토큰이 가리키는 (인증된) 이메일을 조회한다. 조회만 하고 삭제하지 않는다.
     * 만료되었거나 존재하지 않는 토큰이면 empty.
     */
    Optional<String> findVerifiedEmail(String verificationToken);

    /**
     * 토큰을 소비(삭제)한다. 회원가입 완료 시 호출해 재사용을 막는다.
     */
    void consumeVerificationToken(String verificationToken);
}
