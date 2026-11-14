package com.hyunsu.springboot.board.presentation.dto.response;

/**
 * 게시글 구분 선택지 응답.
 * adminOnly인 구분(공지)은 관리자에게만 내려준다.
 */
public record PostCategoryResponse(
        String code,
        String label
) {
}
