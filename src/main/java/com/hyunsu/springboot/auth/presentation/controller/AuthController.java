package com.hyunsu.springboot.auth.presentation.controller;

import com.hyunsu.springboot.auth.application.command.LoginCommandService;
import com.hyunsu.springboot.auth.application.command.LogoutCommandService;
import com.hyunsu.springboot.auth.application.command.TokenReissueCommandService;
import com.hyunsu.springboot.auth.application.command.dto.LoginCommand;
import com.hyunsu.springboot.auth.application.command.dto.LoginCommandResult;
import com.hyunsu.springboot.auth.application.command.dto.ReissueCommandResult;
import com.hyunsu.springboot.auth.presentation.dto.request.LoginRequest;
import com.hyunsu.springboot.auth.presentation.dto.response.LoginResponse;
import com.hyunsu.springboot.auth.presentation.dto.response.ReissueResponse;
import com.hyunsu.springboot.auth.presentation.mapper.AuthMapper;
import com.hyunsu.springboot.global.common.response.ApiResponse;
import com.hyunsu.springboot.global.common.util.IpAddressResolver;
import com.hyunsu.springboot.global.config.properties.AuthCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 인증(Auth) 관련 API.
 * 모든 경로는 "/api"로 시작한다.
 *
 * SecurityConfig에서 /api/auth/** 는 permitAll() 처리되어 있음.
 * (단, /logout은 호출 자체는 막지 않지만, memberId가 없으면 의미가 없으므로
 *  화면에서 로그인 상태일 때만 호출하도록 함)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final LoginCommandService loginCommandService;
    private final TokenReissueCommandService tokenReissueCommandService;
    private final LogoutCommandService logoutCommandService;
    private final AuthMapper authMapper;
    private final AuthCookieProperties authCookieProperties;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String ipAddress = IpAddressResolver.resolve(httpServletRequest);
        LoginCommand command = authMapper.toCommand(request, ipAddress);

        LoginCommandResult result = loginCommandService.login(command);
        LoginResponse response = authMapper.toResponse(result);

        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(result.refreshToken(), Duration.ofDays(14));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(response));
    }

    @PostMapping("/reissue")
    public ApiResponse<ReissueResponse> reissue(HttpServletRequest httpServletRequest) {
        String refreshToken = extractRefreshTokenFromCookie(httpServletRequest);

        ReissueCommandResult result = tokenReissueCommandService.reissue(refreshToken);
        ReissueResponse response = authMapper.toResponse(result);

        return ApiResponse.success(response);
    }

    /**
     * 로그아웃.
     * POST /api/auth/logout
     *
     * Redis의 Refresh Token을 삭제하고, 쿠키도 즉시 만료(maxAge=0)시켜 브라우저에서 제거한다.
     * Access Token 자체는 그 만료시간까지 stateless하게 유효하지만, 재발급이 막히므로
     * 최대 Access Token 만료시간 이내에 완전한 로그아웃 효과가 발생한다.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long memberId) {
        logoutCommandService.logout(memberId);

        return ApiResponse.success();
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(authCookieProperties.secure())
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}