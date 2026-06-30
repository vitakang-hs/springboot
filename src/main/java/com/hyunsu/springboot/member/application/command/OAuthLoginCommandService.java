package com.hyunsu.springboot.member.application.command;

import com.hyunsu.springboot.global.security.crypto.EmailHasher;
import com.hyunsu.springboot.member.application.command.dto.OAuthLoginCommand;
import com.hyunsu.springboot.member.application.command.dto.OAuthLoginCommandResult;
import com.hyunsu.springboot.member.domain.model.entity.Member;
import com.hyunsu.springboot.member.domain.model.entity.MemberOAuth;
import com.hyunsu.springboot.member.domain.repository.MemberOAuthRepository;
import com.hyunsu.springboot.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 로그인(OAuth2) Command 처리.
 *
 * 흐름:
 *  1. (provider, providerId)로 기존 연동(member_oauth) 조회
 *     - 있으면: 그 연동의 memberId를 그대로 사용 (기존 회원 로그인)
 *  2. 연동이 없으면, 이메일 해시로 기존 로컬 회원이 있는지 확인
 *     - 있으면: 그 기존 회원에 새로운 OAuth 연동을 추가 (계정 통합)
 *     - 없으면: 회원을 생성하지 않고 needsSignup=true 결과를 반환한다.
 *       (회원가입은 회원가입 페이지에서 사용자가 직접 진행. 이메일 인증은
 *        이미 구글에서 검증된 이메일이므로 인증 단계만 스킵 처리한다)
 *
 * 비밀번호 검증, 로그인 실패 카운트 등 로컬 로그인(LoginCommandService)의 정책은 적용하지 않는다.
 * (OAuth 인증 자체가 이미 신뢰할 수 있는 외부 인증이므로)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthLoginCommandService {

    private final MemberRepository memberRepository;
    private final MemberOAuthRepository memberOAuthRepository;
    private final EmailHasher emailHasher;

    public OAuthLoginCommandResult login(OAuthLoginCommand command) {
        return memberOAuthRepository
                .findByProviderAndProviderId(command.provider(), command.providerId())
                .map(oauth -> OAuthLoginCommandResult.of(oauth.getMemberId()))
                .orElseGet(() -> findExistingMemberAndLink(command));
    }

    private OAuthLoginCommandResult findExistingMemberAndLink(OAuthLoginCommand command) {
        String emailHash = emailHasher.hash(command.email());

        return memberRepository.findByEmailHash(emailHash)
                .map(member -> {
                    linkOAuth(member.getId(), command);
                    return OAuthLoginCommandResult.of(member.getId());
                })
                .orElseGet(OAuthLoginCommandResult::signupRequired);
    }

    private void linkOAuth(Long memberId, OAuthLoginCommand command) {
        MemberOAuth memberOAuth = MemberOAuth.of(memberId, command.provider(), command.providerId());
        memberOAuthRepository.save(memberOAuth);
    }
}