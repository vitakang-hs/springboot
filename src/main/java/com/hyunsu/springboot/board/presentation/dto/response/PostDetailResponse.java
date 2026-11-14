package com.hyunsu.springboot.board.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 응답.
 *
 * mine: 요청한 회원이 작성자인지 - 화면에서 수정/삭제 버튼 노출 여부를 결정한다.
 *       (버튼을 숨기는 것은 화면 편의일 뿐이며, 실제 권한 검증은 서버의 Command Service가 한다)
 */
public record PostDetailResponse(
        Long postId,
        String category,
        String categoryName,
        String title,
        String content,
        String authorUsername,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean mine,
        List<CommentResponse> comments
) {
}
