package com.hyunsu.springboot.member.domain.repository;

import com.hyunsu.springboot.member.domain.model.entity.MemberOAuth;
import com.hyunsu.springboot.member.domain.model.vo.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberOAuthRepository extends JpaRepository<MemberOAuth, Long> {

    /**
     * 소셜 로그인 시, 이미 연동된 계정인지 확인하기 위한 조회.
     * (provider + provider가 발급한 고유 ID 조합으로 유일하게 식별)
     */
    Optional<MemberOAuth> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}