package com.hyunsu.springboot.member.application.query;

import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 인증 완료 증명 토큰으로 인증된 이메일을 조회한다.
 *
 * 구글 신규 가입자가 회원가입 페이지에 진입했을 때, URL에는 토큰만 담겨 오므로
 * 이 조회로 이메일을 받아 폼에 채운다. (이메일을 URL에 노출하지 않기 위함)
 * 조회만 하고 토큰을 소비하지 않으므로, 새로고침해도 토큰이 유효한 동안은 폼이 복원된다.
 */
@Service
@RequiredArgsConstructor
public class VerifiedEmailQueryService {

    private final EmailVerifier emailVerifier;

    public String getVerifiedEmail(String verificationToken) {
        return emailVerifier.findVerifiedEmail(verificationToken)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED));
    }
}
