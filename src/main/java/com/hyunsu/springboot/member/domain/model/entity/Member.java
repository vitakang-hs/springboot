package com.hyunsu.springboot.member.domain.model.entity;

import com.hyunsu.springboot.member.domain.model.vo.MemberStatus;
import com.hyunsu.springboot.member.domain.model.vo.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

import java.time.LocalDateTime;

/**
 * 회원 (Aggregate Root)
 *
 * member 테이블과 1:1로 매핑된다.
 * setter를 열어두지 않고, 상태 변경은 의도가 드러나는 메서드로만 허용한다.
 *
 * V6 정책 변경: 모든 회원은 username/password를 보유한다.
 * 구글 로그인 시 DB에 일치하는 회원이 없으면 회원을 자동 생성하지 않고
 * 회원가입 페이지로 리다이렉트하여 사용자가 직접 아이디/비밀번호를 입력하게 한다
 * (이메일은 이미 구글에서 검증되었으므로 이메일 인증 단계만 스킵).
 * 이후 구글 로그인은 member_oauth 연동을 통해 기존 회원과 연결될 뿐,
 * username/password 없는 계정은 더 이상 생성되지 않는다.
 *
 * (과거 V3~V5 사이에는 OAuth 전용 무자격 가입(registerByOAuth)을 허용했으나,
 *  V6에서 정책이 변경되며 해당 가입 경로는 제거되었다)
 */
@Entity
@Table(name = "member")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final short MAX_LOGIN_FAIL_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password; // BCrypt 암호화된 값

    @Column(name = "email", nullable = false)
    private String email; // AES 암호화된 값

    @Column(name = "email_hash", nullable = false, length = 64)
    private String emailHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "login_fail_count", nullable = false)
    private short loginFailCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Member(String username, String encodedPassword, String encryptedEmail, String emailHash) {
        this.username = username;
        this.password = encodedPassword;
        this.email = encryptedEmail;
        this.emailHash = emailHash;
        this.role = Role.USER;
        this.status = MemberStatus.ACTIVE;
        this.loginFailCount = 0;
    }

    /**
     * 회원가입 정적 팩토리.
     * 이 메서드는 암호화를 책임지지 않는다 - 호출하는 Service가 이미 암호화된 값을 넘겨야 한다.
     *
     * 일반 가입이든, 구글 인증을 거친 가입이든 이 메서드 하나로 통일된다
     * (V6부터는 모든 가입 경로가 아이디/비밀번호를 필수로 요구하기 때문).
     *
     * @param username        로그인 아이디 (평문)
     * @param encodedPassword BCrypt로 이미 암호화된 비밀번호
     * @param encryptedEmail  AES로 이미 암호화된 이메일
     * @param emailHash       이메일의 SHA-256 해시
     */
    public static Member register(String username, String encodedPassword, String encryptedEmail, String emailHash) {
        if (username == null || encodedPassword == null) {
            throw new IllegalArgumentException("회원가입은 아이디와 비밀번호가 필수입니다.");
        }
        return new Member(username, encodedPassword, encryptedEmail, emailHash);
    }

    // ===== 로그인 관련 행위 =====

    public boolean isLoginable() {
        return this.status == MemberStatus.ACTIVE;
    }

    public void recordLoginFailure() {
        this.loginFailCount++;
        if (this.loginFailCount >= MAX_LOGIN_FAIL_COUNT) {
            this.status = MemberStatus.LOCKED;
        }
    }

    public void recordLoginSuccess() {
        this.loginFailCount = 0;
    }
}