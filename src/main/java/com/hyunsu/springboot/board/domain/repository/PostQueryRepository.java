package com.hyunsu.springboot.board.domain.repository;

import com.hyunsu.springboot.board.application.query.dto.PostListResult;
import com.hyunsu.springboot.board.application.query.dto.PostSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 게시글 목록 조회 전용 Repository 인터페이스.
 *
 * - 쓰기(PostRepository)와 읽기(PostQueryRepository)를 분리하여
 *   QueryDSL 의존이 domain 레이어로 새어나오지 않도록 한다.
 * - 구현체는 infrastructure.repository.querydsl 에 위치한다.
 */
public interface PostQueryRepository {

    /**
     * 게시글 목록 조회.
     * - 공지(isNotice=true)는 항상 상단에 고정된다.
     * - 삭제된 게시글(deletedAt IS NOT NULL)은 제외된다.
     * - categoryCode, keyword 는 null 이면 조건에서 제외된다.
     */
    //Page<PostListResult> findPostList(PostSearchCondition condition, Pageable pageable);
}