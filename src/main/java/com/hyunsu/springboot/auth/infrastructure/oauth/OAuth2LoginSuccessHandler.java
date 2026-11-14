package com.hyunsu.springboot.auth.infrastructure.oauth;

import com.hyunsu.springboot.auth.domain.service.RefreshTokenManager;
import com.hyunsu.springboot.auth.infrastructure.jwt.JwtProvider;
import com.hyunsu.springboot.global.config.properties.AuthCookieProperties;
import com.hyunsu.springboot.member.application.command.OAuthLoginCommandService;
import com.hyunsu.springboot.member.application.command.dto.OAuthLoginCommand;
import com.hyunsu.springboot.member.application.command.dto.OAuthLoginCommandResult;
import com.hyunsu.springboot.member.domain.model.entity.Member;
import com.hyunsu.springboot.member.domain.model.vo.OAuthProvider;
import com.hyunsu.springboot.member.domain.repository.MemberRepository;
import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * 구글 OAuth2 인증 성공 시 호출되는 핸들러.
 *
 * 흐름:
 *  1. 구글이 준 사용자 정보(OAuth2User)에서 email, sub(고유 ID) 추출
 *  2. OAuthLoginCommandService로 회원 조회 처리
 *  3-A. 기존 회원이면: JWT 발급 → /oauth/success로 리다이렉트
 *  3-B. 신규 사용자면: 구글이 검증한 이메일로 "인증 완료 증명 토큰"을 발급(EmailVerifier, TTL 30분)하고
 *       토큰만 URL에 담아 회원가입 페이지로 리다이렉트.
 *       - 이메일은 URL에 노출되지 않는다 (이전에는 Base64로 인코딩해 URL에 담았으나, 디코딩하면 평문이 드러나 변경)
 *       - 이 토큰은 일반 가입의 이메일 인증코드 검증 성공 시 발급되는 것과 같은 토큰이라,
 *         회원가입 API는 가입 경로와 무관하게 같은 방식으로 이메일 인증 여부를 검증한다.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String OAUTH_SUCCESS_REDIRECT_PATH = "/oauth/success";
    private static final String SIGNUP_REDIRECT_PATH = "/signup";

    private final OAuthLoginCommandService oAuthLoginCommandService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenManager refreshTokenManager;
    private final AuthCookieProperties authCookieProperties;
    private final EmailVerifier emailVerifier;

    public OAuth2LoginSuccessHandler(
            OAuthLoginCommandService oAuthLoginCommandService,
            MemberRepository memberRepository,
            JwtProvider jwtProvider,
            RefreshTokenManager refreshTokenManager,
            AuthCookieProperties authCookieProperties,
            EmailVerifier emailVerifier
    ) {
        this.oAuthLoginCommandService = oAuthLoginCommandService;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenManager = refreshTokenManager;
        this.authCookieProperties = authCookieProperties;
        this.emailVerifier = emailVerifier;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getAttribute("sub");

        OAuthLoginCommand command = new OAuthLoginCommand(OAuthProvider.GOOGLE, providerId, email);
        OAuthLoginCommandResult result = oAuthLoginCommandService.login(command);

        if (result.needsSignup()) {
            redirectToSignup(response, email);
            return;
        }

        issueTokensAndRedirect(response, result.memberId());
    }

    /**
     * 신규 사용자 → 인증 완료 증명 토큰을 발급하고, URL에는 토큰만 담아 회원가입 페이지로 리다이렉트.
     * source=google, emailVerified=true 파라미터는 signup.js가 이메일 인증 UI를 숨기기 위한 화면용 표시일 뿐이며,
     * 실제 인증 여부는 회원가입 API가 토큰으로 검증한다.
     */
    private void redirectToSignup(HttpServletResponse response, String email) throws IOException {
        String token = emailVerifier.issueVerificationToken(email);

        String redirectUrl = SIGNUP_REDIRECT_PATH
                + "?token=" + token
                + "&emailVerified=true"
                + "&source=google";

        response.sendRedirect(redirectUrl);
    }

    private void issueTokensAndRedirect(HttpServletResponse response, Long memberId) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("OAuth 로그인 처리 직후 회원을 찾을 수 없습니다."));

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        refreshTokenManager.save(
                member.getId(),
                refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenValidityMillis())
        );

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(authCookieProperties.secure())
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(14))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        response.sendRedirect(OAUTH_SUCCESS_REDIRECT_PATH + "?accessToken=" + accessToken);
    }
}