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
 * Spring Security의 OAuth2 Login은 기본적으로 세션 기반 로그인을 전제로 하지만,
 * 이 프로젝트는 JWT를 사용하므로(STATELESS) 이 핸들러에서 인증 성공 직후
 * 흐름을 가로채 우리 JWT를 발급하고, 화면으로 리다이렉트한다.
 *
 * 흐름:
 *  1. 구글이 준 사용자 정보(OAuth2User)에서 email, sub(고유 ID) 추출
 *  2. OAuthLoginCommandService로 회원 조회/가입 처리
 *  3. Access/Refresh Token 발급, Refresh Token은 Redis 저장 + Cookie 설정
 *  4. Access Token을 쿼리 파라미터로 실어 프론트 페이지로 리다이렉트
 *     (화면이 그 토큰을 받아 메모리에 저장하도록 함)
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String OAUTH_SUCCESS_REDIRECT_PATH = "/oauth/success";

    private final OAuthLoginCommandService oAuthLoginCommandService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenManager refreshTokenManager;
    private final AuthCookieProperties authCookieProperties;

    public OAuth2LoginSuccessHandler(
            OAuthLoginCommandService oAuthLoginCommandService,
            MemberRepository memberRepository,
            JwtProvider jwtProvider,
            RefreshTokenManager refreshTokenManager,
            AuthCookieProperties authCookieProperties
    ) {
        this.oAuthLoginCommandService = oAuthLoginCommandService;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenManager = refreshTokenManager;
        this.authCookieProperties = authCookieProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getAttribute("sub"); // 구글이 발급하는 사용자 고유 식별자

        OAuthLoginCommand command = new OAuthLoginCommand(OAuthProvider.GOOGLE, providerId, email);
        OAuthLoginCommandResult result = oAuthLoginCommandService.login(command);

        Member member = memberRepository.findById(result.memberId())
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