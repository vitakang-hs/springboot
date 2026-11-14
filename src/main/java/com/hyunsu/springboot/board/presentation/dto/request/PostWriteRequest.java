package com.hyunsu.springboot.board.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 작성/수정 요청.
 *
 * category: PostCategory 코드값 (NOTICE, FREE, QNA). 유효성/권한 검증은 PostWriteCommandService가 한다.
 */
public record PostWriteRequest(

        @NotBlank(message = "구분을 선택해 주세요.")
        String category,

        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 200, message = "제목은 200자 이하로 입력해 주세요.")
        String title,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 20000, message = "내용은 20,000자 이하로 입력해 주세요.")
        String content
) {
}
