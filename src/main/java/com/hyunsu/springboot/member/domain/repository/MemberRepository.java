package com.hyunsu.springboot.member.domain.repository;

import com.hyunsu.springboot.member.domain.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Member Aggregate Root에 대한 Repository.
 *
 * Aggregate 설계 원칙에 따라, Member Aggregate에 대한 Repository는 이것 하나만 존재한다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmailHash(String emailHash);

    /**
     * 로그인 시 아이디로 회원 조회.
     */
    Optional<Member> findByUsername(String username);

    /**
     * 소셜 로그인 시, 같은 이메일로 가입된 기존(로컬) 회원이 있는지 확인하기 위한 조회.
     * 있으면 새 계정을 만들지 않고 기존 회원에 OAuth 연동만 추가한다.
     */
    Optional<Member> findByEmailHash(String emailHash);
}