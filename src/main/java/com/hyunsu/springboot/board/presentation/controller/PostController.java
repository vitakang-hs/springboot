package com.hyunsu.springboot.board.presentation.controller;

import com.hyunsu.springboot.board.application.command.CommentWriteCommandService;
import com.hyunsu.springboot.board.application.command.PostViewCountCommandService;
import com.hyunsu.springboot.board.application.command.PostWriteCommandService;
import com.hyunsu.springboot.board.application.query.PostDetailQueryService;
import com.hyunsu.springboot.board.application.query.PostListQueryService;
import com.hyunsu.springboot.board.application.query.dto.PostSearchCondition;
import com.hyunsu.springboot.board.domain.model.vo.PostCategory;
import com.hyunsu.springboot.board.presentation.dto.request.CommentCreateRequest;
import com.hyunsu.springboot.board.presentation.dto.request.PostWriteRequest;
import com.hyunsu.springboot.board.presentation.dto.response.IdResponse;
import com.hyunsu.springboot.board.presentation.dto.response.PostCategoryResponse;
import com.hyunsu.springboot.board.presentation.dto.response.PostDetailResponse;
import com.hyunsu.springboot.board.presentation.dto.response.PostListItemResponse;
import com.hyunsu.springboot.board.presentation.mapper.PostMapper;
import com.hyunsu.springboot.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 게시판 API. 모든 경로는 로그인이 필요하다 (SecurityConfig의 /api/** authenticated).
 *
 * 관리자 여부는 JwtAuthenticationFilter가 채운 권한(ROLE_ADMIN)으로 판단해 Command에 넘긴다.
 * board 도메인은 member의 Role을 직접 알지 않는다 (PostCategory.adminOnly 참고).
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final PostListQueryService postListQueryService;
    private final PostDetailQueryService postDetailQueryService;
    private final PostWriteCommandService postWriteCommandService;
    private final PostViewCountCommandService postViewCountCommandService;
    private final CommentWriteCommandService commentWriteCommandService;
    private final PostMapper postMapper;

    /**
     * 게시글 목록 조회
     *
     * GET /api/posts?page=0&size=15&categoryCode=NOTICE&searchType=TITLE&keyword=공지
     *
     * - page, size 는 Spring Pageable 기본값 적용 (page=0, size=15)
     * - categoryCode 생략 시 전체 조회
     * - searchType: TITLE / CONTENT / TITLE_CONTENT(기본) / AUTHOR
     * - keyword 생략 시 검색 없음
     */
    @GetMapping
    public ApiResponse<Page<PostListItemResponse>> getPostList(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PostSearchCondition condition = PostSearchCondition.of(categoryCode, searchType, keyword);

        Page<PostListItemResponse> result = postListQueryService
                .getPostList(condition, pageable)
                .map(postMapper::toResponse);

        return ApiResponse.success(result);
    }

    /**
     * 작성 가능한 게시글 구분 목록.
     * GET /api/posts/categories
     *
     * 관리자 전용 구분(공지)은 관리자에게만 포함된다. 작성/수정 화면의 select를 채우는 데 쓴다.
     */
    @GetMapping("/categories")
    public ApiResponse<List<PostCategoryResponse>> getWritableCategories(Authentication authentication) {
        boolean admin = isAdmin(authentication);

        List<PostCategoryResponse> categories = Arrays.stream(PostCategory.values())
                .filter(category -> admin || !category.isAdminOnly())
                .map(category -> new PostCategoryResponse(category.name(), category.getLabel()))
                .toList();

        return ApiResponse.success(categories);
    }

    /**
     * 게시글 상세 조회 (댓글 포함). 조회할 때마다 조회수가 1 증가한다.
     * GET /api/posts/{postId}
     *
     * 수정 화면에서 기존 값을 불러올 때는 ?increaseView=false로 조회수 증가를 막는다.
     */
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "true") boolean increaseView,
            @AuthenticationPrincipal Long memberId
    ) {
        if (increaseView) {
            postViewCountCommandService.increase(postId);
        }
        PostDetailResponse response = postMapper.toResponse(postDetailQueryService.getPostDetail(postId), memberId);
        return ApiResponse.success(response);
    }

    /**
     * 게시글 작성.
     * POST /api/posts
     */
    @PostMapping
    public ApiResponse<IdResponse> createPost(
            @RequestBody @Valid PostWriteRequest request,
            @AuthenticationPrincipal Long memberId,
            Authentication authentication
    ) {
        Long postId = postWriteCommandService.create(postMapper.toCommand(request, memberId, isAdmin(authentication)));
        return ApiResponse.success(HttpStatus.CREATED, new IdResponse(postId));
    }

    /**
     * 게시글 수정 (작성자 본인만).
     * PUT /api/posts/{postId}
     */
    @PutMapping("/{postId}")
    public ApiResponse<Void> updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostWriteRequest request,
            @AuthenticationPrincipal Long memberId,
            Authentication authentication
    ) {
        postWriteCommandService.update(postId, postMapper.toCommand(request, memberId, isAdmin(authentication)));
        return ApiResponse.success();
    }

    /**
     * 게시글 삭제 (작성자 본인만, 댓글도 함께 삭제).
     * DELETE /api/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal Long memberId
    ) {
        postWriteCommandService.delete(postId, memberId);
        return ApiResponse.success();
    }

    /**
     * 댓글 작성.
     * POST /api/posts/{postId}/comments
     */
    @PostMapping("/{postId}/comments")
    public ApiResponse<IdResponse> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        Long commentId = commentWriteCommandService.create(postMapper.toCommand(postId, request, memberId));
        return ApiResponse.success(HttpStatus.CREATED, new IdResponse(commentId));
    }

    /**
     * 댓글 삭제 (작성자 본인만).
     * DELETE /api/posts/{postId}/comments/{commentId}
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long memberId
    ) {
        commentWriteCommandService.delete(postId, commentId, memberId);
        return ApiResponse.success();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }
}
