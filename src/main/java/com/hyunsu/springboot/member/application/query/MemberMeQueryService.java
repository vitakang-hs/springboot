package com.hyunsu.springboot.member.application.query;

import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.global.security.crypto.AesGcmCryptoService;
import com.hyunsu.springboot.member.domain.model.entity.Member;
import com.hyunsu.springboot.member.domain.repository.MemberRepository;
import com.hyunsu.springboot.member.presentation.dto.response.MemberMeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인한 본인 정보 조회.
 * "사용 가능 여부 확인"(MemberAvailabilityQueryService)과는 책임이 달라 별도 클래스로 분리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberMeQueryService {

    private final MemberRepository memberRepository;
    private final AesGcmCryptoService aesGcmCryptoService;

    public MemberMeResponse getMe(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String decryptedEmail = aesGcmCryptoService.decrypt(member.getEmail());

        return new MemberMeResponse(member.getId(), member.getUsername(), decryptedEmail);
    }
}