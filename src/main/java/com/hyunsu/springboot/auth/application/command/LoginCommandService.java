package com.hyunsu.springboot.auth.application.command;

import com.hyunsu.springboot.auth.application.command.dto.LoginCommand;
import com.hyunsu.springboot.auth.application.command.dto.LoginCommandResult;
import com.hyunsu.springboot.auth.domain.service.RefreshTokenManager;
import com.hyunsu.springboot.auth.infrastructure.jwt.JwtProvider;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.member.domain.model.entity.Member;
import com.hyunsu.springboot.member.domain.model.entity.MemberHistory;
import com.hyunsu.springboot.member.domain.repository.MemberHistoryRepository;
import com.hyunsu.springboot.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 로그인 Command 처리.
 *
 * 흐름:
 *  1. username으로 회원 조회 (없으면 INVALID_CREDENTIALS - 아이디/비번 중 무엇이 틀렸는지 구분해 알려주지 않음)
 *  2. 계정 상태 확인 (LOCKED/WITHDRAWN이면 거부)
 *  3. Bcrypt 비밀번호 검증
 *     - 실패: 실패 횟수 증가(+ 5회 도달 시 자동 LOCKED), 이력 기록 후 예외
 *     - 성공: 실패 횟수 초기화, 이력 기록
 *  4. Access/Refresh Token 발급, Refresh Token은 Redis에 저장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoginCommandService {

    private final MemberRepository memberRepository;
    private final MemberHistoryRepository memberHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenManager refreshTokenManager;

    public LoginCommandResult login(LoginCommand command) {
        Member member = memberRepository.findByUsername(command.username())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!member.isLoginable()) {
            throw new CustomException(ErrorCode.ACCOUNT_LOCKED);
        }

        boolean passwordMatches = passwordEncoder.matches(command.password(), member.getPassword());

        if (!passwordMatches) {
            member.recordLoginFailure();
            recordHistory(member.getId(), false, "로그인 실패 - 비밀번호 불일치", command.ipAddress());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        member.recordLoginSuccess();
        recordHistory(member.getId(), true, "로그인 성공", command.ipAddress());

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        refreshTokenManager.save(
                member.getId(),
                refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenValidityMillis())
        );

        return new LoginCommandResult(
                accessToken,
                jwtProvider.getAccessTokenValidityMillis() / 1000,
                refreshToken
        );
    }

    private void recordHistory(Long memberId, boolean success, String description, String ipAddress) {
        MemberHistory history = MemberHistory.ofLogin(memberId, success, description, ipAddress);
        memberHistoryRepository.save(history);
    }
}