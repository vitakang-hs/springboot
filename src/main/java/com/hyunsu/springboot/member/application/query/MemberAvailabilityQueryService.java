package com.hyunsu.springboot.member.application.query;

import com.hyunsu.springboot.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 조회(Query) 책임을 담당.
 * CQRS 원칙에 따라 변경(Command)과 분리한다.
 *
 * 이 클래스의 메서드는 데이터를 변경하지 않으므로 클래스 레벨에 readOnly 트랜잭션을 기본 적용한다.
 * (Hibernate가 변경 감지(dirty checking) 로직을 생략해 약간의 성능 이점도 있음)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAvailabilityQueryService {

    private final MemberRepository memberRepository;

    /**
     * 아이디 중복확인.
     * @return true면 이미 사용 중인 아이디(중복), false면 사용 가능
     */
    public boolean isUsernameDuplicated(String username) {
        return memberRepository.existsByUsername(username);
    }
}