package com.hyunsu.springboot.member.application.command;

import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.global.security.crypto.AesGcmCryptoService;
import com.hyunsu.springboot.global.security.crypto.EmailHasher;
import com.hyunsu.springboot.member.application.command.dto.MemberRegisterCommand;
import com.hyunsu.springboot.member.domain.model.entity.Member;
import com.hyunsu.springboot.member.domain.repository.MemberRepository;
import com.hyunsu.springboot.member.domain.service.EmailVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 Command 처리.
 *
 * presentation의 Request DTO를 직접 의존하지 않고, application.command.dto의
 * Command 객체만 받는다 (MemberMapper가 Controller 쪽에서 변환해 전달).
 *
 * 흐름:
 *  1. 이메일 인증 여부 확인 - 인증 완료 증명 토큰이 가리키는 이메일이 가입하려는 이메일과 같은지
 *     (화면(JS)의 인증 상태는 API 직접 호출로 우회할 수 있으므로 서버가 최종 검증)
 *  2. 아이디/이메일 중복 재확인 (화면에서 이미 확인했더라도, DB 변경은 서버가 최종 검증)
 *  3. 비밀번호 Bcrypt 암호화
 *  4. 이메일 AES 암호화 + SHA-256 해시 생성
 *  5. Member.register()로 엔티티 생성 후 저장
 *  6. 인증 완료 증명 토큰 소비 (재사용 방지)
 *     저장이 실패하면(중복 등) 토큰을 남겨두어, 아이디만 바꿔 다시 시도할 때 이메일 재인증이 필요 없게 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MemberRegisterCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AesGcmCryptoService aesGcmCryptoService;
    private final EmailHasher emailHasher;
    private final EmailVerifier emailVerifier;

    public void register(MemberRegisterCommand command) {
        validateEmailVerified(command.emailVerificationToken(), command.email());
        validateNotDuplicated(command.username(), command.email());

        String encodedPassword = passwordEncoder.encode(command.password());
        String encryptedEmail = aesGcmCryptoService.encrypt(command.email());
        String emailHash = emailHasher.hash(command.email());

        Member member = Member.register(command.username(), encodedPassword, encryptedEmail, emailHash);
        memberRepository.save(member);

        emailVerifier.consumeVerificationToken(command.emailVerificationToken());
    }

    private void validateEmailVerified(String verificationToken, String email) {
        String verifiedEmail = emailVerifier.findVerifiedEmail(verificationToken)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED));

        if (!verifiedEmail.equals(email)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    private void validateNotDuplicated(String username, String email) {
        if (memberRepository.existsByUsername(username)) {
            throw new CustomException(ErrorCode.USERNAME_DUPLICATED);
        }

        String emailHash = emailHasher.hash(email);
        if (memberRepository.existsByEmailHash(emailHash)) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED);
        }
    }
}
