package com.hyunsu.springboot.board.application.command;

import com.hyunsu.springboot.board.application.command.dto.CommentCreateCommand;
import com.hyunsu.springboot.board.domain.model.entity.Comment;
import com.hyunsu.springboot.board.domain.model.entity.Post;
import com.hyunsu.springboot.board.domain.repository.CommentRepository;
import com.hyunsu.springboot.board.domain.repository.PostRepository;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentWriteCommandServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 12, 0);

    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;
    CommentWriteCommandService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new CommentWriteCommandService(postRepository, commentRepository, fixedClock);
    }

    @Test
    @DisplayName("살아있는 게시글에는 댓글을 작성할 수 있다")
    void create_success() {
        given(postRepository.findActiveById(10L)).willReturn(Optional.of(Post.create(1L, "FREE", "t", "c")));
        given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

        service.create(new CommentCreateCommand(10L, 2L, "댓글"));

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("삭제되었거나 없는 게시글에는 댓글을 작성할 수 없다")
    void create_postNotFound() {
        given(postRepository.findActiveById(10L)).willReturn(Optional.empty());

        CommentCreateCommand command = new CommentCreateCommand(10L, 2L, "댓글");

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.POST_NOT_FOUND);
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("작성자는 자기 댓글을 삭제할 수 있다")
    void delete_byOwner() {
        Comment comment = Comment.create(10L, 2L, "댓글");
        given(commentRepository.findActiveByIdAndPostId(5L, 10L)).willReturn(Optional.of(comment));

        service.delete(10L, 5L, 2L);

        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDeletedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("다른 사람의 댓글은 삭제할 수 없다")
    void delete_byOther_forbidden() {
        Comment comment = Comment.create(10L, 2L, "댓글");
        given(commentRepository.findActiveByIdAndPostId(5L, 10L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.delete(10L, 5L, 3L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_FORBIDDEN);
        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("다른 게시글의 댓글 ID로는 삭제할 수 없다")
    void delete_commentOfAnotherPost() {
        given(commentRepository.findActiveByIdAndPostId(5L, 99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, 5L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }
}
