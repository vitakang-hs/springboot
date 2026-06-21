package com.hyunsu.springboot.global.security;

import jakarta.servlet.Filter;

/**
 * SecurityFilterChain에 끼워 넣을 인증 필터를 제공하는 추상화.
 * SecurityConfig(global)는 이 인터페이스에만 의존하고, 실제 인증 방식이
 * JWT라는 사실은 auth.infrastructure.jwt.JwtAuthenticationFilterProvider만 알고 있다.
 * (의존성 역전 - global이 특정 도메인의 구현 기술을 직접 알지 않도록)
 * 인증 방식이 JWT가 아닌 다른 방식으로 바뀌어도, SecurityConfig는 수정할 필요가 없다.
 */
public interface AuthenticationTokenFilterProvider {

    Filter getFilter();
}