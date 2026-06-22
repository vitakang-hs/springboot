package com.hyunsu.springboot.board.presentation.mapper;

import com.hyunsu.springboot.board.application.query.dto.PostListResult;
import com.hyunsu.springboot.board.presentation.dto.response.PostListItemResponse;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostListItemResponse toResponse(PostListResult result) {
        return new PostListItemResponse(
                result.postId(),
                result.categoryCode(),
                result.categoryName(),
                result.title(),
                result.authorUsername(),
                result.commentCount(),
                result.isNotice(),
                result.createdAt()
        );
    }
}