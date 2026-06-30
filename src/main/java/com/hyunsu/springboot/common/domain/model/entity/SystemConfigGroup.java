package com.hyunsu.springboot.common.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 설정 그룹 (Aggregate Root).
 *
 * 관리자 설정 페이지의 좌측 메뉴(타이틀) 단위를 표현한다. (예: 로그인, 게시판)
 * group_code를 PK로 사용한다 (예: LOGIN, BOARD - 코드 자체가 의미를 가지는 식별자).
 *
 * V4의 code_group/code 공통코드 테이블을 대체하는 V7 마이그레이션으로 신설되었다.
 * code_group과 달리, 이 그룹은 "관리자가 화면에서 직접 새 그룹을 추가"하는 것을
 * 상정하지 않는다 - 그룹이 늘어나는 시점은 새로운 설정 카테고리가 코드 레벨에서
 * 추가되는 시점(예: "이메일" 그룹 신설)이며, 이때 항상 마이그레이션이 함께 따른다.
 * 즉 이 테이블의 row 자체는 "관리자가 자유롭게 추가하는 대상"이 아니라
 * "개발 시점에 정의되는 메뉴 구조"이고, 화면에서 수정 가능한 것은 각 그룹에 속한
 * SystemConfigItem의 value뿐이다.
 */
@Entity
@Table(name = "system_config_group")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemConfigGroup {

    @Id
    @Column(name = "group_code", length = 50)
    private String groupCode;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}