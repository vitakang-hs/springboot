package com.hyunsu.springboot.auth.application.command;

import com.hyunsu.springboot.auth.application.command.dto.ReissueCommandResult;
import com.hyunsu.springboot.auth.domain.service.RefreshTokenManager;
import com.hyunsu.springboot.auth.infrastructure.jwt.JwtProvider;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.member.domain.model.entity.Member;
import com.hyunsu.springboot.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Access Token 재발급 Command 처리.
 * 흐름:
 *  1. Refresh Token이 JWT로서 유효한지 검증 (서명/만료)
 *  2. 토큰에서 memberId 추출
 *  3. Redis에 저장된 Refresh Token과 일치하는지 확인
 *     (불일치하면 다른 곳에서 로그아웃했거나, 탈취된 토큰일 수 있으므로 거부)
 *  4. 회원 상태 재확인 (그 사이 LOCKED/WITHDRAWN 되었을 수 있음)
 *  5. 새 Access Token 발급 (Refresh Token은 재사용 - Rotation은 추후 적용 예정)
 * 로그인(LoginCommandService)과 분리한 이유: 호출 시점(사용자 행위 vs 시스템 자동 트리거)과
 * 부수효과(로그인 실패 카운트, 이력 기록 등)가 달라 책임이 명확히 구분되는 별개의 유스케이스이다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenReissueCommandService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenManager refreshTokenManager;

    public ReissueCommandResult reissue(String refreshToken) {
        if (refreshToken == null || !jwtProvider.isValid(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtProvider.getMemberId(refreshToken);

        if (!refreshTokenManager.isValid(memberId, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.isLoginable()) {
            throw new CustomException(ErrorCode.ACCOUNT_LOCKED);
        }

        String newAccessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());

        return new ReissueCommandResult(newAccessToken, jwtProvider.getAccessTokenValidityMillis() / 1000);
    }
}