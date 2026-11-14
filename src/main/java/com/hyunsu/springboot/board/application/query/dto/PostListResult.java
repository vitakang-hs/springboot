package com.hyunsu.springboot.board.application.query.dto;

import java.time.LocalDateTime;

/**
 * 게시글 목록 조회 결과.
 * QueryDSL Projections.constructor() 로 직접 매핑된다.
 *
 * category는 PostCategory enum의 name() 코드값 그대로를 담는다 (예: "NOTICE").
 * 화면에 보여줄 라벨("공지")로의 변환은 이 객체의 책임이 아니라,
 * presentation.mapper.PostMapper가 PostCategory.valueOf(category)로 변환해
 * Response DTO에 채운다 (V7 이전에는 code 테이블 LEFT JOIN으로 가져왔으나,
 * 공통코드 폐기 이후 enum 매핑 방식으로 전환).
 */
public record PostListResult(
        Long postId,
        String category,        // 카테고리 코드 (NOTICE, FREE, QNA)
        String title,
        String authorUsername,  // member.username
        long commentCount,
        int viewCount,
        boolean isNotice,       // category = 'NOTICE' 여부
        LocalDateTime createdAt
) {}