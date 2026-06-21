package com.hyunsu.springboot.member.domain.model.entity;

import com.hyunsu.springboot.member.domain.model.vo.MemberHistoryType;
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
 * 회원 이력 (member_history 테이블과 매핑).
 *
 * 로그인 이력과 회원정보 변경 이력을 type 컬럼으로 구분해 하나의 테이블에서 관리한다.
 * Member Aggregate에 속하지만, 별도 Aggregate Root로 다루지 않고 Member를 통해서만 생성한다고
 * 보기보다는 - 이력은 "벌어진 사실의 기록"이라 Member의 현재 상태와는 독립적으로 추가되는
 * 로그성 데이터이므로, 이 프로젝트에서는 MemberHistoryRepository를 통해 직접 저장한다.
 * (Comment처럼 부모 Aggregate를 통해서만 생성해야 하는 강한 종속 관계는 아님)
 */
@Entity
@Table(name = "member_history")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MemberHistoryType type;

    @Column(name = "success")
    private Boolean success; // LOGIN 전용

    @Column(name = "field_name", length = 50)
    private String fieldName; // MODIFY 전용

    @Column(name = "description")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private MemberHistory(Long memberId, MemberHistoryType type, Boolean success,
                          String fieldName, String description, String ipAddress) {
        this.memberId = memberId;
        this.type = type;
        this.success = success;
        this.fieldName = fieldName;
        this.description = description;
        this.ipAddress = ipAddress;
    }

    /**
     * 로그인 이력 생성.
     */
    public static MemberHistory ofLogin(Long memberId, boolean success, String description, String ipAddress) {
        return new MemberHistory(memberId, MemberHistoryType.LOGIN, success, null, description, ipAddress);
    }

    /**
     * 정보 변경 이력 생성. (추후 비밀번호 변경, 이메일 변경 등에서 사용 예정)
     */
    public static MemberHistory ofModify(Long memberId, String fieldName, String description, String ipAddress) {
        return new MemberHistory(memberId, MemberHistoryType.MODIFY, null, fieldName, description, ipAddress);
    }
}