package com.hyunsu.springboot.member.domain.model.vo;

/**
 * 회원 이력 유형.
 * DB의 member_history.type 컬럼(VARCHAR)과 매핑된다.
 */
public enum MemberHistoryType {
    LOGIN,
    MODIFY
}