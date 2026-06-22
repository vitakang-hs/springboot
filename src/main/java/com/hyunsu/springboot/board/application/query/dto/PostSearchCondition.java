package com.hyunsu.springboot.board.application.query.dto;

/**
 * 게시글 목록 검색 조건.
 * null 인 필드는 조건에서 제외된다 (QueryDSL where 절에서 null-safe 처리).
 */
public record PostSearchCondition(
        String categoryCode,  // null 이면 전체 카테고리
        String keyword        // null 이면 검색 없음 (제목+내용 OR 검색)
) {
    public static PostSearchCondition of(String categoryCode, String keyword) {
        String trimmedKeyword = (keyword != null && keyword.isBlank()) ? null : keyword;
        return new PostSearchCondition(categoryCode, trimmedKeyword);
    }
}