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
 * 댓글 (Post Aggregate 내부).
 *
 * 게시글(Post) 없이는 독립적으로 존재할 수 없으므로 Post Aggregate 경계 안에 포함.
 * MemberHistory/MemberOAuth와 동일하게 별도 CommentRepository로 직접 저장한다.
 */
@Entity
@Table(name = "comment")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Comment(Long postId, Long memberId, String content) {
        this.postId = postId;
        this.memberId = memberId;
        this.content = content;
    }

    public static Comment create(Long postId, Long memberId, String content) {
        return new Comment(postId, memberId, content);
    }

    // ===== 행위 메서드 =====

    public void update(String content) {
        this.content = content;
    }

    /**
     * soft delete. 현재 시각은 호출하는 Service가 Clock으로 구해 넘긴다 (엔티티는 Clock을 주입받지 않음).
     */
    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }
}