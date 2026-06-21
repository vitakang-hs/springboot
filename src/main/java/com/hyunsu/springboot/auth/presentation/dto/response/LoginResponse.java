package com.hyunsu.springboot.auth.presentation.dto.response;

/**
 * 로그인 성공 응답.
 * Access Token만 Body로 전달한다. Refresh Token은 HttpOnly Cookie로 별도 전달되어
 * 응답 Body에는 포함되지 않는다 (XSS로부터 보호하기 위함).
 */
public record LoginResponse(
        String accessToken,
        long accessTokenExpiresIn // 초 단위
) {
}