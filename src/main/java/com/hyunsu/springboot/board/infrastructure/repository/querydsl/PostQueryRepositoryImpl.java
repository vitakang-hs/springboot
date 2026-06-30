package com.hyunsu.springboot.board.infrastructure.repository.querydsl;

import com.hyunsu.springboot.board.application.query.dto.PostListResult;
import com.hyunsu.springboot.board.application.query.dto.PostSearchCondition;
import com.hyunsu.springboot.board.domain.model.vo.PostCategory;
import com.hyunsu.springboot.board.domain.repository.PostQueryRepository;
import com.hyunsu.springboot.board.domain.model.entity.QPost;
import com.hyunsu.springboot.board.domain.model.entity.QComment;
import com.hyunsu.springboot.member.domain.model.entity.QMember;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository {

    private static final String NOTICE_CATEGORY = PostCategory.NOTICE.name();

    private final JPAQueryFactory queryFactory;

    private static final QPost post = QPost.post;
    private static final QComment comment = QComment.comment;
    private static final QMember member = QMember.member;

    @Override
    public Page<PostListResult> findPostList(PostSearchCondition condition, Pageable pageable) {

        // 1. 댓글 수 서브쿼리
        var commentCount = com.querydsl.jpa.JPAExpressions
                .select(comment.count())
                .from(comment)
                .where(
                        comment.postId.eq(post.id),
                        comment.deletedAt.isNull()
                );

        // 2. 목록 조회
        //    - member INNER JOIN: post.member_id는 FK로 member.id를 항상 가리키며
        //      (탈퇴 회원도 soft delete라 row 자체는 남아있음), V6부터 모든 회원이
        //      username을 보유하므로(OAuth 전용 무자격 가입 경로 제거) author가 없는
        //      게시글은 존재할 수 없다. 그 보장을 INNER JOIN으로 명확히 표현한다.
        //    - 카테고리명은 더 이상 DB 조인으로 가져오지 않는다 (V7부터 PostCategory enum으로 관리).
        //      여기서는 category 코드값만 내려주고, 라벨 변환은 presentation.mapper.PostMapper가 담당한다.
        //    - 공지 여부: category = 'NOTICE' 로 판단 (별도 컬럼 없음)
        List<PostListResult> content = queryFactory
                .select(Projections.constructor(PostListResult.class,
                        post.id,
                        post.category,
                        post.title,
                        member.username,
                        commentCount,
                        post.category.eq(NOTICE_CATEGORY),
                        post.createdAt
                ))
                .from(post)
                .innerJoin(member).on(member.id.eq(post.memberId))
                .where(
                        post.deletedAt.isNull(),
                        categoryEq(condition.categoryCode()),
                        keywordContains(condition.keyword())
                )
                .orderBy(noticeFirst(), post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3. 전체 카운트
        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.deletedAt.isNull(),
                        categoryEq(condition.categoryCode()),
                        keywordContains(condition.keyword())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // 공지 상단 고정: category = 'NOTICE' 인 게시글을 먼저
    private OrderSpecifier<Integer> noticeFirst() {
        return post.category.eq(NOTICE_CATEGORY)
                .when(true).then(0)
                .otherwise(1)
                .asc();
    }

    // 카테고리 코드 필터 (null 이면 전체)
    private BooleanExpression categoryEq(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) return null;
        return post.category.eq(categoryCode);
    }

    // 제목 + 내용 OR 검색 (null 이면 전체)
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }
}