package com.hyunsu.springboot.board.infrastructure.repository.querydsl;

import com.hyunsu.springboot.board.application.query.dto.CommentResult;
import com.hyunsu.springboot.board.application.query.dto.PostDetailResult;
import com.hyunsu.springboot.board.application.query.dto.PostListResult;
import com.hyunsu.springboot.board.application.query.dto.PostSearchCondition;
import com.hyunsu.springboot.board.application.query.dto.PostSearchType;
import com.hyunsu.springboot.board.domain.model.entity.QComment;
import com.hyunsu.springboot.board.domain.model.entity.QPost;
import com.hyunsu.springboot.board.domain.model.vo.PostCategory;
import com.hyunsu.springboot.board.domain.repository.PostQueryRepository;
import com.hyunsu.springboot.member.domain.model.entity.QMember;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
        var commentCount = JPAExpressions
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
        //    - 카테고리명은 DB 조인으로 가져오지 않는다 (V7부터 PostCategory enum으로 관리).
        //      여기서는 category 코드값만 내려주고, 라벨 변환은 presentation.mapper.PostMapper가 담당한다.
        //    - 공지 여부: category = 'NOTICE' 로 판단 (별도 컬럼 없음)
        List<PostListResult> content = queryFactory
                .select(Projections.constructor(PostListResult.class,
                        post.id,
                        post.category,
                        post.title,
                        member.username,
                        commentCount,
                        post.viewCount,
                        post.category.eq(NOTICE_CATEGORY),
                        post.createdAt
                ))
                .from(post)
                .innerJoin(member).on(member.id.eq(post.memberId))
                .where(
                        post.deletedAt.isNull(),
                        categoryEq(condition.categoryCode()),
                        keywordMatches(condition.searchType(), condition.keyword())
                )
                .orderBy(noticeFirst(), post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3. 전체 카운트 (작성자 검색 조건이 member를 참조하므로 같은 JOIN을 건다)
        Long total = queryFactory
                .select(post.count())
                .from(post)
                .innerJoin(member).on(member.id.eq(post.memberId))
                .where(
                        post.deletedAt.isNull(),
                        categoryEq(condition.categoryCode()),
                        keywordMatches(condition.searchType(), condition.keyword())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Optional<PostDetailResult> findPostDetail(Long postId) {
        Tuple row = queryFactory
                .select(post.id, post.category, post.title, post.content, post.memberId,
                        member.username, post.viewCount, post.createdAt, post.updatedAt)
                .from(post)
                .innerJoin(member).on(member.id.eq(post.memberId))
                .where(
                        post.id.eq(postId),
                        post.deletedAt.isNull()
                )
                .fetchOne();

        if (row == null) {
            return Optional.empty();
        }

        // 댓글은 findComments()로 따로 조회해 PostDetailQueryService가 채운다
        return Optional.of(new PostDetailResult(
                row.get(post.id),
                row.get(post.category),
                row.get(post.title),
                row.get(post.content),
                row.get(post.memberId),
                row.get(member.username),
                row.get(post.viewCount),
                row.get(post.createdAt),
                row.get(post.updatedAt),
                List.of()
        ));
    }

    @Override
    public List<CommentResult> findComments(Long postId) {
        return queryFactory
                .select(Projections.constructor(CommentResult.class,
                        comment.id,
                        comment.memberId,
                        member.username,
                        comment.content,
                        comment.createdAt
                ))
                .from(comment)
                .innerJoin(member).on(member.id.eq(comment.memberId))
                .where(
                        comment.postId.eq(postId),
                        comment.deletedAt.isNull()
                )
                .orderBy(comment.createdAt.asc(), comment.id.asc())
                .fetch();
    }

    // 공지 상단 고정: category = 'NOTICE' 인 게시글을 먼저
    // (CASE WHEN category = 'NOTICE' THEN 0 ELSE 1 END. boolean식.when(true) 형태는
    //  "CASE (category = ?) WHEN true ..."로 변환되어 Hibernate 7 HQL 파서가 거부하므로 CaseBuilder를 쓴다)
    private OrderSpecifier<Integer> noticeFirst() {
        return new CaseBuilder()
                .when(post.category.eq(NOTICE_CATEGORY)).then(0)
                .otherwise(1)
                .asc();
    }

    // 카테고리 코드 필터 (null 이면 전체)
    private BooleanExpression categoryEq(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) return null;
        return post.category.eq(categoryCode);
    }

    // 검색 대상별 키워드 조건 (keyword가 null 이면 전체)
    private BooleanExpression keywordMatches(PostSearchType searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        return switch (searchType) {
            case TITLE -> post.title.containsIgnoreCase(keyword);
            case CONTENT -> post.content.containsIgnoreCase(keyword);
            case AUTHOR -> member.username.containsIgnoreCase(keyword);
            case TITLE_CONTENT -> post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword));
        };
    }
}
