package com.hyunsu.springboot.member.domain.model.entity;

import com.hyunsu.springboot.member.domain.model.vo.OAuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

import java.time.LocalDateTime;

/**
 * 회원의 소셜 로그인(OAuth2) 연동 정보 (member_oauth 테이블과 매핑).
 *
 * Member와 연관되지만, MemberHistory와 동일한 이유로 Member Aggregate에 강하게
 * 종속시키지 않고 MemberOAuthRepository를 통해 직접 저장한다.
 * (한 회원이 여러 provider를 연동할 수 있어 "연동 기록"에 가까운 성격)
 */
@Entity
@Table(name = "member_oauth")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private MemberOAuth(Long memberId, OAuthProvider provider, String providerId) {
        this.memberId = memberId;
        this.provider = provider;
        this.providerId = providerId;
    }

    /**
     * 소셜 로그인 연동 생성.
     */
    public static MemberOAuth of(Long memberId, OAuthProvider provider, String providerId) {
        return new MemberOAuth(memberId, provider, providerId);
    }
}