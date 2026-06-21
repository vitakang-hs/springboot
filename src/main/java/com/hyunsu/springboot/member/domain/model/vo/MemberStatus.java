package com.hyunsu.springboot.member.domain.model.vo;

/**
 * 계정 상태.
 * DB의 member.status 컬럼(VARCHAR)과 매핑된다.
 */
public enum MemberStatus {
    ACTIVE,     // 정상
    LOCKED,     // 로그인 실패 5회 초과 등으로 잠김
    WITHDRAWN   // 탈퇴 (soft delete)
}