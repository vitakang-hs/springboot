package com.hyunsu.springboot.auth.infrastructure.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authorization 헤더의 "Bearer {token}"을 검증하고, 유효하면 SecurityContext에
 * 인증 정보(Authentication)를 채워 넣는 필터.
 *
 * UsernamePasswordAuthenticationFilter보다 앞에 등록해서, 이 필터를 통과한 요청은
 * 이후 authorizeHttpRequests()의 authenticated() 규칙을 만족시킬 수 있도록 한다.
 *
 * 토큰이 없거나 유효하지 않으면 그냥 다음 필터로 넘긴다 (여기서 401을 직접 내리지 않음).
 * 최종적으로 인증이 필요한 요청인데 SecurityContext가 비어있으면,
 * Spring Security가 자체적으로 401을 응답한다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtProvider.isValid(token)) {
            Long memberId = jwtProvider.getMemberId(token);
            String role = jwtProvider.getRole(token);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // principal로는 memberId(Long)를 그대로 사용.
            // 추후 Member 엔티티 전체가 필요해지면 별도 UserDetails 구현체로 교체 고려.
            var authentication = new UsernamePasswordAuthenticationToken(memberId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}