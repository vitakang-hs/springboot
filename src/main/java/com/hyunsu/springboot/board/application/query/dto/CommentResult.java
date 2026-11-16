package com.hyunsu.springboot.board.application.query.dto;

import java.time.LocalDateTime;

/**
 * 게시글 상세의 댓글 한 건 (QueryDSL Projections.constructor()로 매핑).
 */
public record CommentResult(
        Long commentId,
        Long authorId,
        String authorUsername,
        String content,
        LocalDateTime createdAt
) {
}
