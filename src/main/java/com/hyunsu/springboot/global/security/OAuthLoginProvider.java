package com.hyunsu.springboot.global.security;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * SecurityConfig의 .oauth2Login(...) 설정에 끼워 넣을 구성요소를 제공하는 추상화.
 *
 * SecurityConfig(global)는 이 인터페이스에만 의존하고, OAuth2 사용자 정보를 어떻게 처리하는지,
 * 인증 성공 후 어떤 방식(JWT 등)으로 응답하는지는 auth.infrastructure.oauth.GoogleOAuthLoginProvider만 알고 있다.
 * (의존성 역전 - global이 특정 도메인의 구현 기술을 직접 알지 않도록, JwtAuthenticationFilterProvider와 동일한 패턴)
 */
public interface OAuthLoginProvider {

    OAuth2UserService<OAuth2UserRequest, OAuth2User> getUserService();

    AuthenticationSuccessHandler getSuccessHandler();
}