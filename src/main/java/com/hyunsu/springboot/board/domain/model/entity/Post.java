package com.hyunsu.springboot.board.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 게시글 (Aggregate Root).
 *
 * post 테이블과 1:1 매핑. Comment는 이 Aggregate에 종속되지만,
 * 별도 CommentRepository를 통해 직접 저장한다.
 * (MemberHistory/MemberOAuth와 같은 패턴)
 *
 * category는 code 테이블의 POST_CATEGORY 그룹 코드값(예: NOTICE, FREE)을 저장.
 * DB FK 제약 없이 애플리케이션 레이어에서 유효성을 검증한다.
 */
@Entity
@Table(name = "post")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Post(Long memberId, String category, String title, String content) {
        this.memberId = memberId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
    }

    /**
     * 게시글 작성 정적 팩토리.
     *
     * @param memberId 작성자 ID
     * @param category 게시글 구분 코드 (POST_CATEGORY 그룹의 코드값)
     * @param title    제목
     * @param content  내용
     */
    public static Post create(Long memberId, String category, String title, String content) {
        return new Post(memberId, category, title, content);
    }

    // ===== 행위 메서드 =====

    public void update(String category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }
}