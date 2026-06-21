package com.hyunsu.springboot.auth.application.command.dto;

/**
 * 토큰 재발급 Command 처리 결과.
 * 재발급 시에는 Refresh Token을 그대로 재사용하므로(Rotation 미적용),
 * Cookie를 다시 갱신할 필요가 없다 - 새 Access Token만 반환하면 된다.
 */
public record ReissueCommandResult(
        String accessToken,
        long accessTokenExpiresIn
) {
}