package com.hyunsu.springboot.board.application.query.dto;

import java.time.LocalDateTime;

/**
 * 게시글 목록 조회 결과.
 * QueryDSL Projections.constructor() 로 직접 매핑된다.
 */
public record PostListResult(
        Long postId,
        String categoryCode,
        String categoryName,   // code 테이블 조인으로 가져옴
        String title,
        String authorUsername,
        long commentCount,
        boolean isNotice,
        LocalDateTime createdAt
) {}