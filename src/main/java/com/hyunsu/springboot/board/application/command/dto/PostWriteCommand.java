package com.hyunsu.springboot.board.application.command.dto;

/**
 * 게시글 작성/수정 Command.
 *
 * @param writerId 요청한 회원 ID (작성 시 작성자, 수정 시 본인 확인용)
 * @param admin    요청한 회원이 관리자인지 (관리자 전용 카테고리 작성 가능 여부 판단)
 * @param category 게시글 구분 코드 (PostCategory.name())
 */
public record PostWriteCommand(
        Long writerId,
        boolean admin,
        String category,
        String title,
        String content
) {
}
