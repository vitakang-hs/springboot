package com.hyunsu.springboot.member.application.command.dto;

import com.hyunsu.springboot.member.domain.model.vo.OAuthProvider;

/**
 * 소셜 로그인 Command.
 * OAuth2 제공자(구글 등)로부터 받은 사용자 정보를 담는다.
 */
public record OAuthLoginCommand(
        OAuthProvider provider,
        String providerId, // 제공자가 발급하는 사용자 고유 식별자 (구글: sub)
        String email
) {
}