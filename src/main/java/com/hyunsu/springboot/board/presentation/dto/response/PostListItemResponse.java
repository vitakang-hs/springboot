package com.hyunsu.springboot.board.presentation.dto.response;

import java.time.LocalDateTime;

/**
 * 게시글 목록 단건 응답.
 * list.html 의 테이블 한 행에 대응된다.
 */
public record PostListItemResponse(
        Long postId,
        String categoryCode,
        String categoryName,
        String title,
        String authorUsername,
        long commentCount,
        boolean isNotice,
        LocalDateTime createdAt
) {}