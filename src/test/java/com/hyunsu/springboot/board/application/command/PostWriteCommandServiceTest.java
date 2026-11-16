package com.hyunsu.springboot.board.application.command;

import com.hyunsu.springboot.board.application.command.dto.PostWriteCommand;
import com.hyunsu.springboot.board.domain.model.entity.Post;
import com.hyunsu.springboot.board.domain.repository.CommentRepository;
import com.hyunsu.springboot.board.domain.repository.PostRepository;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostWriteCommandServiceTest {

    private static final Long WRITER = 1L;
    private static final Long OTHER = 2L;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 12, 0);

    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;
    PostWriteCommandService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new PostWriteCommandService(postRepository, commentRepository, fixedClock);
    }

    private PostWriteCommand command(Long writerId, boolean admin, String category) {
        return new PostWriteCommand(writerId, admin, category, "제목", "내용");
    }

    private Post savedPost(Long id, Long writerId, String category) {
        Post post = Post.create(writerId, category, "기존 제목", "기존 내용");
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    @Test
    @DisplayName("일반 회원은 자유 게시글을 작성할 수 있다")
    void create_free() {
        given(postRepository.save(any(Post.class))).willAnswer(inv -> {
            Post post = inv.getArgument(0);
            ReflectionTestUtils.setField(post, "id", 10L);
            return post;
        });

        Long postId = service.create(command(WRITER, false, "FREE"));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(postId).isEqualTo(10L);
        assertThat(captor.getValue().getMemberId()).isEqualTo(WRITER);
        assertThat(captor.getValue().getCategory()).isEqualTo("FREE");
    }

    @Test
    @DisplayName("일반 회원은 공지를 작성할 수 없다")
    void create_notice_byUser_forbidden() {
        PostWriteCommand notice = command(WRITER, false, "NOTICE");

        assertThatThrownBy(() -> service.create(notice))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.POST_CATEGORY_ADMIN_ONLY);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("관리자는 공지를 작성할 수 있다")
    void create_notice_byAdmin() {
        given(postRepository.save(any(Post.class))).willAnswer(inv -> inv.getArgument(0));

        service.create(command(WRITER, true, "NOTICE"));

        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("존재하지 않는 구분 코드로는 작성할 수 없다")
    void create_invalidCategory() {
        PostWriteCommand unknown = command(WRITER, false, "UNKNOWN");

        assertThatThrownBy(() -> service.create(unknown))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.POST_CATEGORY_INVALID);
    }

    @Test
    @DisplayName("작성자는 자기 글을 수정할 수 있다")
    void update_byOwner() {
        Post post = savedPost(10L, WRITER, "FREE");
        given(postRepository.findActiveById(10L)).willReturn(Optional.of(post));

        service.update(10L, new PostWriteCommand(WRITER, false, "QNA", "새 제목", "새 내용"));

        assertThat(post.getCategory()).isEqualTo("QNA");
        assertThat(post.getTitle()).isEqualTo("새 제목");
        assertThat(post.getContent()).isEqualTo("새 내용");
    }

    @Test
    @DisplayName("다른 사람의 글은 수정할 수 없다")
    void update_byOther_forbidden() {
        Post post = savedPost(10L, WRITER, "FREE");
        given(postRepository.findActiveById(10L)).willReturn(Optional.of(post));

        PostWriteCommand byOther = command(OTHER, false, "FREE");

        assertThatThrownBy(() -> service.update(10L, byOther))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.POST_FORBIDDEN);
        assertThat(post.getTitle()).isEqualTo("기존 제목");
    }

    @Test
    @DisplayName("삭제되었거나 없는 글은 수정할 수 없다")
    void update_notFound() {
        given(postRepository.findActiveById(10L)).willReturn(Optional.empty());

        PostWriteCommand update = command(WRITER, false, "FREE");

        assertThatThrownBy(() -> service.update(10L, update))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("글을 삭제하면 soft delete 되고 댓글도 함께 삭제된다")
    void delete_byOwner_alsoDeletesComments() {
        Post post = savedPost(10L, WRITER, "FREE");
        given(postRepository.findActiveById(10L)).willReturn(Optional.of(post));

        service.delete(10L, WRITER);

        assertThat(post.isDeleted()).isTrue();
        assertThat(post.getDeletedAt()).isEqualTo(NOW);
        verify(commentRepository).softDeleteAllByPostId(10L, NOW);
    }

    @Test
    @DisplayName("다른 사람의 글은 삭제할 수 없다")
    void delete_byOther_forbidden() {
        Post post = savedPost(10L, WRITER, "FREE");
        given(postRepository.findActiveById(10L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> service.delete(10L, OTHER))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.POST_FORBIDDEN);
        assertThat(post.isDeleted()).isFalse();
        verify(commentRepository, never()).softDeleteAllByPostId(anyLong(), any());
    }
}
