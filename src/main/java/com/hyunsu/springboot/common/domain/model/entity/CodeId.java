package com.hyunsu.springboot.common.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * code 테이블의 복합 PK (group_code, code).
 * JPA @EmbeddedId 사용 시 Serializable 구현과 equals/hashCode가 필수.
 */
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class CodeId implements Serializable {

    @Column(name = "group_code", length = 50)
    private String groupCode;

    @Column(name = "code", length = 50)
    private String code;

    public CodeId(String groupCode, String code) {
        this.groupCode = groupCode;
        this.code = code;
    }
}