package com.hyunsu.springboot.board.domain.model.vo;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 게시글 카테고리.
 *
 * V7 이전에는 code_group/code 공통코드 테이블로 관리했으나,
 * "코드를 자유롭게 추가해도 결국 그 코드를 사용하는 로직은 개발자가 작성해야 한다"는
 * 한계로 인해 공통코드 테이블을 폐기하고 enum 상수로 단순화했다.
 *
 * post.category 컬럼에는 이 enum의 name()이 그대로 저장된다 (VARCHAR(50)).
 *
 * member 도메인의 Role을 직접 참조하지 않는다 (도메인 간 결합 방지 - Aggregate 설계 원칙).
 * 대신 "관리자 전용 카테고리인지" 여부만 board 도메인 스스로 표현하고(adminOnly),
 * 이 값을 실제 회원의 Role과 비교해 작성 가능 여부를 판단하는 책임은
 * board 도메인을 호출하는 Service(application 계층)가 진다.
 */
@Getter
public enum PostCategory {

    NOTICE("공지", true),
    FREE("자유", false),
    QNA("질문", false);

    private final String label;
    private final boolean adminOnly; // true면 관리자만 작성 가능

    PostCategory(String label, boolean adminOnly) {
        this.label = label;
        this.adminOnly = adminOnly;
    }

    /**
     * 공지 여부.
     * 게시글 목록에서 상단 고정 정렬, 노출 개수 제한(NOTICE_MAX_COUNT 설정값) 판단에 사용한다.
     */
    public boolean isNotice() {
        return this == NOTICE;
    }

    /**
     * 코드값(name)으로 카테고리를 찾는다. 존재하지 않는 코드면 empty.
     * (valueOf는 잘못된 값에 IllegalArgumentException을 던지므로, 사용자 입력 검증에는 이 메서드를 쓴다)
     */
    public static Optional<PostCategory> fromCode(String code) {
        return Arrays.stream(values())
                .filter(category -> category.name().equals(code))
                .findFirst();
    }
}