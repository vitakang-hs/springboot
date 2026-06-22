package com.hyunsu.springboot.board.presentation.controller;

import com.hyunsu.springboot.board.application.query.PostListQueryService;
import com.hyunsu.springboot.board.application.query.dto.PostSearchCondition;
import com.hyunsu.springboot.board.presentation.dto.response.PostListItemResponse;
import com.hyunsu.springboot.board.presentation.mapper.PostMapper;
import com.hyunsu.springboot.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostListQueryService postListQueryService;
    private final PostMapper postMapper;

    /**
     * 게시글 목록 조회
     *
     * GET /api/posts?page=0&size=15&categoryCode=NOTICE&keyword=공지
     *
     * - page, size 는 Spring Pageable 기본값 적용 (page=0, size=15)
     * - categoryCode 생략 시 전체 조회
     * - keyword 생략 시 검색 없음
     */
    @GetMapping
    public ApiResponse<Page<PostListItemResponse>> getPostList(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.of(categoryCode, keyword);

        Page<PostListItemResponse> result = postListQueryService
                .getPostList(condition, pageable)
                .map(postMapper::toResponse);

        return ApiResponse.success(result);
    }
}