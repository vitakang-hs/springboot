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
 * username, password는 nullable이다 (V3 마이그레이션).
 * 소셜 로그인(registerByOAuth)으로만 가입한 회원은 아이디/비밀번호 자체가 없으며,
 * 로그인은 항상 소셜 로그인 버튼으로만 가능하다 (로컬 로그인 폼으로는 애초에 접근 불가).
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

    @Column(name = "username", length = 20)
    private String username; // 소셜 로그인 전용 가입자는 null

    @Column(name = "password", length = 100)
    private String password; // BCrypt 암호화된 값. 소셜 로그인 전용 가입자는 null

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
     * 일반(아이디/비밀번호) 회원가입 정적 팩토리.
     * 이 메서드는 암호화를 책임지지 않는다 - 호출하는 Service가 이미 암호화된 값을 넘겨야 한다.
     *
     * @param username        로그인 아이디 (평문)
     * @param encodedPassword BCrypt로 이미 암호화된 비밀번호
     * @param encryptedEmail  AES로 이미 암호화된 이메일
     * @param emailHash       이메일의 SHA-256 해시
     */
    public static Member register(String username, String encodedPassword, String encryptedEmail, String emailHash) {
        if (username == null || encodedPassword == null) {
            throw new IllegalArgumentException("일반 회원가입은 아이디와 비밀번호가 필수입니다.");
        }
        return new Member(username, encodedPassword, encryptedEmail, emailHash);
    }

    /**
     * 소셜 로그인(OAuth2) 전용 회원가입 정적 팩토리.
     * username, password 없이 이메일 정보만으로 가입한다.
     *
     * @param encryptedEmail AES로 이미 암호화된 이메일
     * @param emailHash      이메일의 SHA-256 해시
     */
    public static Member registerByOAuth(String encryptedEmail, String emailHash) {
        return new Member(null, null, encryptedEmail, emailHash);
    }

    /**
     * 소셜 로그인 전용 계정인지 여부.
     * (아이디/비밀번호 자체가 없는 계정)
     */
    public boolean isOAuthOnly() {
        return this.username == null && this.password == null;
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