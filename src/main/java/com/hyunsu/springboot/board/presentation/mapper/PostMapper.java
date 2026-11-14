package com.hyunsu.springboot.board.presentation.mapper;

import com.hyunsu.springboot.board.application.command.dto.CommentCreateCommand;
import com.hyunsu.springboot.board.application.command.dto.PostWriteCommand;
import com.hyunsu.springboot.board.application.query.dto.CommentResult;
import com.hyunsu.springboot.board.application.query.dto.PostDetailResult;
import com.hyunsu.springboot.board.application.query.dto.PostListResult;
import com.hyunsu.springboot.board.domain.model.vo.PostCategory;
import com.hyunsu.springboot.board.presentation.dto.request.CommentCreateRequest;
import com.hyunsu.springboot.board.presentation.dto.request.PostWriteRequest;
import com.hyunsu.springboot.board.presentation.dto.response.CommentResponse;
import com.hyunsu.springboot.board.presentation.dto.response.PostDetailResponse;
import com.hyunsu.springboot.board.presentation.dto.response.PostListItemResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * board presentation ↔ application 타입 변환.
 *  - Request DTO → Command
 *  - Result(application 내부 표현) → Response DTO (카테고리 라벨, "내 글인지" 여부 계산)
 */
@Component
public class PostMapper {

    public PostListItemResponse toResponse(PostListResult result) {
        return new PostListItemResponse(
                result.postId(),
                result.category(),
                labelOf(result.category()),
                result.title(),
                result.authorUsername(),
                result.commentCount(),
                result.viewCount(),
                result.isNotice(),
                result.createdAt()
        );
    }

    public PostDetailResponse toResponse(PostDetailResult result, Long viewerId) {
        return new PostDetailResponse(
                result.postId(),
                result.category(),
                labelOf(result.category()),
                result.title(),
                result.content(),
                result.authorUsername(),
                result.viewCount(),
                result.createdAt(),
                result.updatedAt(),
                Objects.equals(result.authorId(), viewerId),
                result.comments().stream()
                        .map(comment -> toResponse(comment, viewerId))
                        .toList()
        );
    }

    public PostWriteCommand toCommand(PostWriteRequest request, Long memberId, boolean admin) {
        return new PostWriteCommand(memberId, admin, request.category(), request.title(), request.content());
    }

    public CommentCreateCommand toCommand(Long postId, CommentCreateRequest request, Long memberId) {
        return new CommentCreateCommand(postId, memberId, request.content());
    }

    private CommentResponse toResponse(CommentResult result, Long viewerId) {
        return new CommentResponse(
                result.commentId(),
                result.authorUsername(),
                result.content(),
                result.createdAt(),
                Objects.equals(result.authorId(), viewerId)
        );
    }

    // DB에 enum에 없는 옛 코드값이 남아있어도 화면이 깨지지 않도록 코드값 그대로 표시
    private String labelOf(String categoryCode) {
        return PostCategory.fromCode(categoryCode)
                .map(PostCategory::getLabel)
                .orElse(categoryCode);
    }
}
