package com.hyunsu.springboot.board.application.query.dto;

/**
 * 게시글 목록 검색 조건.
 * null 인 필드는 조건에서 제외된다 (QueryDSL where 절에서 null-safe 처리).
 */
public record PostSearchCondition(
        String categoryCode,       // null 이면 전체 카테고리
        PostSearchType searchType, // 검색 대상 (keyword가 있을 때만 의미 있음)
        String keyword             // null 이면 검색 없음
) {
    public static PostSearchCondition of(String categoryCode, String searchType, String keyword) {
        String trimmedCategory = (categoryCode != null && categoryCode.isBlank()) ? null : categoryCode;
        String trimmedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return new PostSearchCondition(trimmedCategory, PostSearchType.fromOrDefault(searchType), trimmedKeyword);
    }
}
