package com.hyunsu.springboot.auth.infrastructure.oauth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;

/**
 * 구글 등 OAuth2 제공자로부터 사용자 정보를 가져오는 지점.
 *
 * Spring Security 표준 흐름(DefaultOAuth2UserService.loadUser)을 그대로 상속해 사용한다.
 * 받아온 사용자 정보(email, sub 등)를 우리 Member/MemberOAuth와 연결하는 실제 비즈니스 로직은
 * 여기가 아니라 OAuth2LoginSuccessHandler에서 처리한다.
 *
 * 지금은 동작을 바꾸지 않으므로 loadUser()를 재정의하지 않는다. 제공자별 사용자 정보 가공이
 * 필요해지면(예: 카카오의 중첩 응답 구조) 이 클래스에서 loadUser()를 재정의한다.
 * (GoogleOAuthLoginProvider가 이 타입을 주입받아 SecurityConfig에 연결하므로 클래스 자체는 유지)
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
}
