package com.hyunsu.springboot.board.presentation.dto.response;

import java.time.LocalDateTime;

/**
 * 게시글 목록 단건 응답.
 * list.html 의 테이블 한 행에 대응된다.
 *
 * category: 코드값 (NOTICE, FREE, QNA) - 프론트에서 필터 등에 활용 가능
 * categoryName: 화면 표시용 라벨 ("공지", "자유", "질문") - PostMapper가 PostCategory enum에서 채운다
 */
public record PostListItemResponse(
        Long postId,
        String category,
        String categoryName,
        String title,
        String authorUsername,
        long commentCount,
        int viewCount,
        boolean isNotice,
        LocalDateTime createdAt
) {}