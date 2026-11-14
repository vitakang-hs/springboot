package com.hyunsu.springboot.board.application.command.dto;

/**
 * 댓글 작성 Command.
 */
public record CommentCreateCommand(
        Long postId,
        Long writerId,
        String content
) {
}
