package com.hyunsu.springboot.board.application.command;

import com.hyunsu.springboot.board.application.command.dto.CommentCreateCommand;
import com.hyunsu.springboot.board.domain.model.entity.Comment;
import com.hyunsu.springboot.board.domain.repository.CommentRepository;
import com.hyunsu.springboot.board.domain.repository.PostRepository;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글 작성/삭제 Command 처리.
 *
 * Comment는 Post Aggregate에 종속되므로, 작성 시 게시글이 살아있는지 먼저 확인한다
 * (삭제된 게시글에는 댓글을 달 수 없음). 삭제는 작성자 본인만 가능하다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommentWriteCommandService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * @return 생성된 댓글 ID
     */
    public Long create(CommentCreateCommand command) {
        postRepository.findActiveById(command.postId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.create(command.postId(), command.writerId(), command.content());
        return commentRepository.save(comment).getId();
    }

    public void delete(Long postId, Long commentId, Long memberId) {
        Comment comment = commentRepository.findActiveByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isOwner(memberId)) {
            throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
        }
        comment.delete();
    }
}
