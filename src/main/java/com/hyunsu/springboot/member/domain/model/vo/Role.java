package com.hyunsu.springboot.member.domain.model.vo;

/**
 * 회원 권한.
 * DB의 member.role 컬럼(VARCHAR)과 매핑된다.
 */
public enum Role {
    USER,
    ADMIN
}