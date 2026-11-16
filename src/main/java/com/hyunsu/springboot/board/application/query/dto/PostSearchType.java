package com.hyunsu.springboot.board.application.query.dto;

import java.util.Arrays;

/**
 * 게시글 목록 검색 대상.
 * 화면의 검색 select(제목/내용/제목+내용/작성자)와 1:1로 대응된다.
 */
public enum PostSearchType {
    TITLE,
    CONTENT,
    TITLE_CONTENT,
    AUTHOR;

    /**
     * 잘못되거나 비어있는 값이면 기본값(제목+내용)으로 처리한다.
     */
    public static PostSearchType fromOrDefault(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(TITLE_CONTENT);
    }
}
