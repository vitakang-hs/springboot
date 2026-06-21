package com.hyunsu.springboot.auth.infrastructure.oauth;

import com.hyunsu.springboot.global.security.OAuthLoginProvider;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * OAuthLoginProvider의 구글 구현체.
 * CustomOAuth2UserService(사용자 정보 처리)와 OAuth2LoginSuccessHandler(JWT 발급/리다이렉트)를
 * SecurityConfig에 제공한다.
 */
@Component
public class GoogleOAuthLoginProvider implements OAuthLoginProvider {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public GoogleOAuthLoginProvider(
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler
    ) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Override
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> getUserService() {
        return customOAuth2UserService;
    }

    @Override
    public AuthenticationSuccessHandler getSuccessHandler() {
        return oAuth2LoginSuccessHandler;
    }
}