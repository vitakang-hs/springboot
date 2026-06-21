package com.hyunsu.springboot.auth.application.command;

import com.hyunsu.springboot.auth.domain.service.RefreshTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 로그아웃 Command 처리.
 * Redis에 저장된 Refresh Token을 삭제해 즉시 무효화한다.
 * (Access Token 자체는 만료 전까지 stateless하게 유효하지만, 재발급이 막히므로
 *  최대 Access Token 만료시간 이내에 완전히 로그아웃 효과가 발생한다)
 */
@Service
@RequiredArgsConstructor
public class LogoutCommandService {

    private final RefreshTokenManager refreshTokenManager;

    public void logout(Long memberId) {
        refreshTokenManager.delete(memberId);
    }
}