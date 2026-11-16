package com.hyunsu.springboot.board.domain.repository;

import com.hyunsu.springboot.board.domain.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 게시글의 삭제되지 않은 댓글 목록 (작성 순).
     */
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findActiveByPostId(Long postId);

    /**
     * 게시글의 삭제되지 않은 댓글 수.
     * 목록 페이지에서 [N] 표시에 사용.
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.deletedAt IS NULL")
    long countActiveByPostId(Long postId);

    /**
     * 게시글에 속한 삭제되지 않은 댓글 단건 조회.
     * postId까지 함께 확인해, 다른 게시글의 댓글 ID로 삭제를 시도하는 요청을 막는다.
     */
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.postId = :postId AND c.deletedAt IS NULL")
    Optional<Comment> findActiveByIdAndPostId(Long commentId, Long postId);

    /**
     * 게시글 삭제 시 댓글도 함께 soft delete (Post Aggregate 불변식).
     */
    @Modifying
    @Query("UPDATE Comment c SET c.deletedAt = :deletedAt WHERE c.postId = :postId AND c.deletedAt IS NULL")
    int softDeleteAllByPostId(Long postId, LocalDateTime deletedAt);
}