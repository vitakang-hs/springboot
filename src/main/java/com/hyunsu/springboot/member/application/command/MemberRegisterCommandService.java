package com.hyunsu.springboot.member.application.command;

import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import com.hyunsu.springboot.global.security.crypto.AesGcmCryptoService;
import com.hyunsu.springboot.global.security.crypto.EmailHasher;
import com.hyunsu.springboot.member.application.command.dto.MemberRegisterCommand;
import com.hyunsu.springboot.member.domain.model.entity.Member;
import com.hyunsu.springboot.member.domain.repository.MemberRepository;
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
 *  1. 아이디/이메일 중복 재확인 (화면에서 이미 확인했더라도, DB 변경은 서버가 최종 검증)
 *  2. 비밀번호 Bcrypt 암호화
 *  3. 이메일 AES 암호화 + SHA-256 해시 생성
 *  4. Member.register()로 엔티티 생성 후 저장
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MemberRegisterCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AesGcmCryptoService aesGcmCryptoService;
    private final EmailHasher emailHasher;

    public void register(MemberRegisterCommand command) {
        validateNotDuplicated(command.username(), command.email());

        String encodedPassword = passwordEncoder.encode(command.password());
        String encryptedEmail = aesGcmCryptoService.encrypt(command.email());
        String emailHash = emailHasher.hash(command.email());

        Member member = Member.register(command.username(), encodedPassword, encryptedEmail, emailHash);
        memberRepository.save(member);
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