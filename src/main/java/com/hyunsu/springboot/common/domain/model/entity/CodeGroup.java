package com.hyunsu.springboot.common.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 공통코드 그룹 (code_group 테이블).
 *
 * group_code가 PK. 예: POST_CATEGORY, MEMBER_STATUS
 * 코드 그룹은 운영 중에 자주 바뀌지 않는 마스터 데이터라
 * setter를 열지 않고 불변에 가깝게 관리한다.
 */
@Entity
@Table(name = "code_group")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeGroup {

    @Id
    @Column(name = "group_code", length = 50)
    private String groupCode;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "description")
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}