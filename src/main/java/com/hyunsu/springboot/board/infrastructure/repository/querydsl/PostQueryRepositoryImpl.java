//package com.hyunsu.springboot.board.infrastructure.repository.querydsl;
//
//import com.hyunsu.springboot.board.application.query.dto.PostListResult;
//import com.hyunsu.springboot.board.application.query.dto.PostSearchCondition;
//import com.hyunsu.springboot.board.domain.repository.PostQueryRepository;
//import com.hyunsu.springboot.board.domain.model.entity.QPost;
//import com.hyunsu.springboot.board.domain.model.entity.QComment;
//import com.hyunsu.springboot.common.domain.model.entity.QCode;
//import com.querydsl.core.types.OrderSpecifier;
//import com.querydsl.core.types.Projections;
//import com.querydsl.core.types.dsl.BooleanExpression;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//@RequiredArgsConstructor
//public class PostQueryRepositoryImpl implements PostQueryRepository {
//
//    private final JPAQueryFactory queryFactory;
//
//    private static final QPost post = QPost.post;
//    private static final QComment comment = QComment.comment;
//    private static final QCode code = QCode.code;
//
//    @Override
//    public Page<PostListResult> findPostList(PostSearchCondition condition, Pageable pageable) {
//
//        // 1. 댓글 수 서브쿼리
//        var commentCount = com.querydsl.jpa.JPAExpressions
//                .select(comment.count())
//                .from(comment)
//                .where(
//                        comment.postId.eq(post.id),
//                        comment.deletedAt.isNull()
//                );
//
//        // 2. 목록 조회
//        List<PostListResult> content = queryFactory
//                .select(Projections.constructor(PostListResult.class,
//                        post.id,
//                        post.categoryCode,
//                        code.codeName,       // code 테이블 조인으로 카테고리명
//                        post.title,
//                        post.authorUsername,
//                        commentCount,
//                        post.isNotice,
//                        post.createdAt
//                ))
//                .from(post)
//                .leftJoin(code)
//                .on(code.id.groupCode.eq("POST_CATEGORY")
//                        .and(code.id.code.eq(post.categoryCode)))
//                .where(
//                        post.deletedAt.isNull(),
//                        categoryCodeEq(condition.categoryCode()),
//                        keywordContains(condition.keyword())
//                )
//                .orderBy(noticeFirst(), post.createdAt.desc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        // 3. 전체 카운트 (페이지네이션 total 계산용)
//        Long total = queryFactory
//                .select(post.count())
//                .from(post)
//                .where(
//                        post.deletedAt.isNull(),
//                        categoryCodeEq(condition.categoryCode()),
//                        keywordContains(condition.keyword())
//                )
//                .fetchOne();
//
//        return new PageImpl<>(content, pageable, total == null ? 0 : total);
//    }
//
//    // 공지 상단 고정: isNotice=true 를 먼저
//    private OrderSpecifier<Integer> noticeFirst() {
//        return post.isNotice
//                .when(true).then(0)
//                .otherwise(1)
//                .asc();
//    }
//
//    // 카테고리 코드 필터 (null 이면 전체)
//    private BooleanExpression categoryCodeEq(String categoryCode) {
//        if (categoryCode == null || categoryCode.isBlank()) return null;
//        return post.categoryCode.eq(categoryCode);
//    }
//
//    // 제목 + 내용 OR 검색 (null 이면 전체)
//    private BooleanExpression keywordContains(String keyword) {
//        if (keyword == null || keyword.isBlank()) return null;
//        return post.title.containsIgnoreCase(keyword)
//                .or(post.content.containsIgnoreCase(keyword));
//    }
//}