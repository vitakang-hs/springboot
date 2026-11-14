package com.hyunsu.springboot.board.presentation.dto.response;

import java.time.LocalDateTime;

/**
 * 댓글 한 건 응답. mine: 요청한 회원이 작성자인지 (삭제 버튼 노출용).
 */
public record CommentResponse(
        Long commentId,
        String authorUsername,
        String content,
        LocalDateTime createdAt,
        boolean mine
) {
}
