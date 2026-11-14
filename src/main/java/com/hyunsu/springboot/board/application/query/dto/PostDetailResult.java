package com.hyunsu.springboot.board.application.query.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회 결과 (application 내부 표현).
 *
 * authorId는 "내 글인지"(수정/삭제 버튼 노출) 판단용으로만 쓰이며,
 * 클라이언트에는 PostMapper가 boolean(mine)으로 변환해 내려준다.
 */
public record PostDetailResult(
        Long postId,
        String category,
        String title,
        String content,
        Long authorId,
        String authorUsername,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentResult> comments
) {

    public PostDetailResult withComments(List<CommentResult> comments) {
        return new PostDetailResult(postId, category, title, content, authorId, authorUsername,
                viewCount, createdAt, updatedAt, comments);
    }
}
