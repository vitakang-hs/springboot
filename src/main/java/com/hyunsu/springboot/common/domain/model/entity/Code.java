package com.hyunsu.springboot.common.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통코드 (code 테이블).
 *
 * 복합 PK: (group_code, code). 예: (POST_CATEGORY, NOTICE), (POST_CATEGORY, FREE)
 *
 * category 같은 코드 컬럼에 FK 제약을 걸지 않고, Service 레이어에서 유효성을 검증한다.
 * (FK를 걸면 코드 변경 시 참조 무결성 제약이 너무 강해지는 문제가 있음)
 */
@Entity
@Table(name = "code")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Code {

    @EmbeddedId
    private CodeId id;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * group_code를 꺼낼 때 id를 통해 접근하지 않아도 되도록 편의 메서드 제공.
     */
    public String getGroupCode() {
        return id.getGroupCode();
    }

    public String getCode() {
        return id.getCode();
    }
}