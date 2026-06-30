package com.hyunsu.springboot.member.application.command.dto;

/**
 * OAuthLoginCommand 처리 결과.
 *
 * - 기존 회원(연동 이력 있음, 또는 이메일로 기존 로컬 회원과 통합)이면
 *   memberId가 채워지고 needsSignup=false.
 * - DB에 일치하는 회원이 전혀 없으면 회원을 생성하지 않고
 *   memberId=null, needsSignup=true 로 반환한다.
 *   (이 경우 호출자는 회원가입 페이지로 리다이렉트해야 한다)
 *
 * 주의: 정적 팩토리 메서드 이름을 record 컴포넌트명(needsSignup)과
 * 동일하게 짓지 않는다. 동일하게 지으면 자바 컴파일러가 이를
 * 컴포넌트의 접근자(accessor) 재정의 시도로 해석해 반환 타입 불일치
 * 컴파일 에러가 발생한다 (accessor는 반드시 boolean을 반환해야 함).
 */
public record OAuthLoginCommandResult(
        Long memberId,
        boolean needsSignup
) {
    public static OAuthLoginCommandResult of(Long memberId) {
        return new OAuthLoginCommandResult(memberId, false);
    }

    public static OAuthLoginCommandResult signupRequired() {
        return new OAuthLoginCommandResult(null, true);
    }
}