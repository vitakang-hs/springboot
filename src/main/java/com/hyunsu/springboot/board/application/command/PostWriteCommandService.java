package com.hyunsu.springboot.board.application.command;

import com.hyunsu.springboot.board.application.command.dto.PostWriteCommand;
import com.hyunsu.springboot.board.domain.model.entity.Post;
import com.hyunsu.springboot.board.domain.model.vo.PostCategory;
import com.hyunsu.springboot.board.domain.repository.CommentRepository;
import com.hyunsu.springboot.board.domain.repository.PostRepository;
import com.hyunsu.springboot.global.exception.CustomException;
import com.hyunsu.springboot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 게시글 작성/수정/삭제 Command 처리.
 *
 * 세 유스케이스 모두 "작성자가 자기 글을 다루는 행위"로 검증 규칙(카테고리 권한, 본인 확인)을 공유해 한 클래스에 둔다.
 * 조회수 증가처럼 성격이 다른 부수효과는 PostViewCountCommandService로 분리했다.
 *
 * 권한 규칙:
 *  - 관리자 전용 카테고리(PostCategory.adminOnly, 예: 공지)는 관리자만 작성/변경할 수 있다.
 *    PostCategory는 member의 Role을 모르므로, Role → admin 여부 판단은 Controller가 하고 Command로 전달한다.
 *  - 수정/삭제는 작성자 본인만 가능하다.
 *  - 삭제 시 댓글도 함께 soft delete 한다 (Post Aggregate 불변식).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PostWriteCommandService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * @return 생성된 게시글 ID
     */
    public Long create(PostWriteCommand command) {
        PostCategory category = validateCategory(command.category(), command.admin());

        Post post = Post.create(command.writerId(), category.name(), command.title(), command.content());
        return postRepository.save(post).getId();
    }

    public void update(Long postId, PostWriteCommand command) {
        Post post = findOwnPost(postId, command.writerId());
        PostCategory category = validateCategory(command.category(), command.admin());

        post.update(category.name(), command.title(), command.content());
    }

    public void delete(Long postId, Long memberId) {
        Post post = findOwnPost(postId, memberId);

        post.delete();
        commentRepository.softDeleteAllByPostId(postId, LocalDateTime.now());
    }

    private Post findOwnPost(Long postId, Long memberId) {
        Post post = postRepository.findActiveById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.isOwner(memberId)) {
            throw new CustomException(ErrorCode.POST_FORBIDDEN);
        }
        return post;
    }

    private PostCategory validateCategory(String code, boolean admin) {
        PostCategory category = PostCategory.fromCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_CATEGORY_INVALID));

        if (category.isAdminOnly() && !admin) {
            throw new CustomException(ErrorCode.POST_CATEGORY_ADMIN_ONLY);
        }
        return category;
    }
}
