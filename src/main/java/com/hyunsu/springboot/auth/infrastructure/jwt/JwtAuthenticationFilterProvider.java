package com.hyunsu.springboot.auth.infrastructure.jwt;

import com.hyunsu.springboot.global.security.AuthenticationTokenFilterProvider;
import jakarta.servlet.Filter;
import org.springframework.stereotype.Component;

/**
 * AuthenticationTokenFilterProvider의 JWT 구현체.
 * JwtAuthenticationFilter(OncePerRequestFilter)를 생성해 SecurityConfig에 제공한다.
 */
@Component
public class JwtAuthenticationFilterProvider implements AuthenticationTokenFilterProvider {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilterProvider(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Filter getFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }
}