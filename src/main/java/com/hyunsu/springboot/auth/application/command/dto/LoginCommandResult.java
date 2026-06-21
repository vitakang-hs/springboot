package com.hyunsu.springboot.auth.application.command.dto;

/**
 * LoginCommand 처리 결과.
 *
 * application 레이어는 presentation 레이어의 타입(LoginResponse 등)을 직접 의존하지 않는다.
 * 그래서 이 객체는 순수한 원시값만 담고, "이 값들을 어떤 응답 DTO로 포장할지"는
 * Controller(또는 Mapper)의 책임으로 둔다.
 *
 * Controller가 accessToken은 응답 Body로, refreshToken은 HttpOnly Cookie로
 * 서로 다른 곳에 나눠 담아야 해서, Service가 둘을 함께 반환할 필요가 있다.
 */
public record LoginCommandResult(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken
) {
}