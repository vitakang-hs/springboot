package com.hyunsu.springboot.auth.presentation.dto.response;

/**
 * 토큰 재발급 성공 응답.
 * 새로 발급된 Access Token만 Body로 전달한다.
 * (Refresh Token은 재사용하며 재발급 시 Cookie를 다시 갱신하지 않음 - Rotation 미적용)
 */
public record ReissueResponse(
        String accessToken,
        long accessTokenExpiresIn
) {
}