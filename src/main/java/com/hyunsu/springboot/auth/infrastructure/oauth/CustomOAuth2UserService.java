package com.hyunsu.springboot.auth.infrastructure.oauth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * 구글 등 OAuth2 제공자로부터 사용자 정보를 가져오는 지점.
 *
 * Spring Security 표준 흐름(DefaultOAuth2UserService)을 그대로 사용한다.
 * 받아온 사용자 정보(email, sub 등)를 우리 Member/MemberOAuth와 연결하는 실제 비즈니스 로직은
 * 여기가 아니라 OAuth2LoginSuccessHandler에서 처리한다.
 * (이 클래스는 Spring Security의 OAuth2UserService 인터페이스 구현 자체가 표준 확장 지점이라
 *  별도로 도메인 인터페이스를 추가로 씌우지 않는다)
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest);
    }
}